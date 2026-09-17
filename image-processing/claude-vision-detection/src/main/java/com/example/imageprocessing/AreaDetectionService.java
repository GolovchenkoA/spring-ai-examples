package com.example.imageprocessing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;

import tools.jackson.databind.ObjectMapper;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

/**
 * Case 1: scan a photo of a self-checkout station and identify areas
 * (bagging, scanning, table) as a JSON description - the source image
 * itself is never modified. Uses Claude's vision capability via Anthropic.
 */
@Service
public class AreaDetectionService {

	private final ChatClient chatClient;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public AreaDetectionService(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}

	public AreaDetectionResult detect(String imagePath) throws IOException {
		File imageFile = new File(imagePath);
		if (!imageFile.isFile()) {
			throw new IOException("Image not found: " + imageFile.getAbsolutePath());
		}

		// Measure the real pixel dimensions locally - vision models are not
		// reliable at reporting exact image dimensions themselves.
		BufferedImage sourceImage = ImageIO.read(imageFile);
		if (sourceImage == null) {
			throw new IOException("Could not read image (unsupported format?): " + imageFile.getAbsolutePath());
		}
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();

		MimeType mimeType = guessMimeType(imageFile.getName());

		String prompt = """
				You are analyzing a photo of a self-checkout station in a retail store.
				The image is exactly %d x %d pixels. Use a top-left origin: x increases
				to the right, y increases downward - the same convention as screen/image
				coordinates.

				Identify these area types if you can actually see them in the photo:
				- "bagging": where scanned items are bagged
				- "scanning": where items are scanned/weighed
				- "table": any checkout counter/table surface

				For each area you can identify, provide a pixel bounding box (x, y, width,
				height) that tightly encloses it, calibrated to the exact image size given
				above. Only include areas you can actually see - do not invent areas that
				aren't visible in the photo. This is a best-effort visual estimate, not a
				precision measurement.
				""".formatted(width, height);

		ModelAreaResponse response = chatClient.prompt()
				.user(u -> u.text(prompt).media(mimeType, new FileSystemResource(imageFile)))
				.call()
				.entity(ModelAreaResponse.class);

		return new AreaDetectionResult(new ImageDimensions(width, height), response.areas());
	}

	public void detectAndWrite(String imagePath, String outputJsonPath) throws IOException {
		AreaDetectionResult result = detect(imagePath);

		Path outputPath = outputJsonPath != null
				? Path.of(outputJsonPath)
				: Path.of(imagePath + ".areas.json");

		objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputPath.toFile(), result);

		System.out.println("Detected " + result.areas().size() + " area(s):");
		for (Area area : result.areas()) {
			System.out.printf("  - %-10s x=%d y=%d width=%d height=%d%n", area.name(), area.x(), area.y(),
					area.width(), area.height());
		}
		System.out.println("\nWritten to: " + outputPath.toAbsolutePath());
	}

	private MimeType guessMimeType(String fileName) {
		String lower = fileName.toLowerCase();
		if (lower.endsWith(".png")) {
			return MimeType.valueOf("image/png");
		}
		if (lower.endsWith(".webp")) {
			return MimeType.valueOf("image/webp");
		}
		if (lower.endsWith(".gif")) {
			return MimeType.valueOf("image/gif");
		}
		return MimeType.valueOf("image/jpeg");
	}

}
