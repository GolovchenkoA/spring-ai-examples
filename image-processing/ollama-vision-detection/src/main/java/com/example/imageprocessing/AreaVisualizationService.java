package com.example.imageprocessing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

import tools.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

/**
 * Case 2: takes the source image plus a Case 1 AreaDetectionResult JSON file
 * and draws the predicted areas onto a NEW annotated image (the original is
 * never modified), so predictions can be visually verified.
 */
@Service
public class AreaVisualizationService {

	private static final Map<String, Color> COLORS_BY_AREA = Map.of(
			"bagging", new Color(0, 170, 0),
			"scanning", new Color(220, 0, 0),
			"table", new Color(0, 100, 220));
	private static final Color DEFAULT_COLOR = new Color(200, 120, 0);

	private final ObjectMapper objectMapper = new ObjectMapper();

	public File visualize(String imagePath, String jsonPath, String outputImagePath) throws IOException {
		File imageFile = new File(imagePath);
		File jsonFile = new File(jsonPath);

		if (!imageFile.isFile()) {
			throw new IOException("Image not found: " + imageFile.getAbsolutePath());
		}
		if (!jsonFile.isFile()) {
			throw new IOException("Areas JSON not found: " + jsonFile.getAbsolutePath());
		}

		AreaDetectionResult result = objectMapper.readValue(jsonFile, AreaDetectionResult.class);

		BufferedImage sourceImage = ImageIO.read(imageFile);
		if (sourceImage == null) {
			throw new IOException("Could not read image (unsupported format?): " + imageFile.getAbsolutePath());
		}

		if (sourceImage.getWidth() != result.sourceImage().width()
				|| sourceImage.getHeight() != result.sourceImage().height()) {
			System.out.printf(
					"WARNING: JSON was generated for a %dx%d image, but %s is %dx%d - "
							+ "coordinates may not line up correctly.%n",
					result.sourceImage().width(), result.sourceImage().height(), imageFile.getName(),
					sourceImage.getWidth(), sourceImage.getHeight());
		}

		BufferedImage annotated = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = annotated.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(sourceImage, 0, 0, null);
		g.setStroke(new BasicStroke(3f));
		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));

		for (Area area : result.areas()) {
			Color color = COLORS_BY_AREA.getOrDefault(area.name().toLowerCase(), DEFAULT_COLOR);
			g.setColor(color);
			g.drawRect(area.x(), area.y(), area.width(), area.height());

			int labelY = Math.max(area.y() - 6, 16);
			g.setColor(new Color(0, 0, 0, 160));
			int labelWidth = g.getFontMetrics().stringWidth(area.name()) + 8;
			g.fillRect(area.x(), labelY - 16, labelWidth, 20);
			g.setColor(color);
			g.drawString(area.name(), area.x() + 4, labelY);
		}
		g.dispose();

		Path outputPath = outputImagePath != null
				? Path.of(outputImagePath)
				: Path.of(withSuffix(imagePath, "-annotated"));

		String format = outputPath.toString().toLowerCase().endsWith(".jpg")
				|| outputPath.toString().toLowerCase().endsWith(".jpeg") ? "jpg" : "png";
		writeImage(annotated, format, outputPath.toFile());

		System.out.println("Annotated image written to: " + outputPath.toAbsolutePath());
		tryOpen(outputPath.toFile());

		return outputPath.toFile();
	}

	/**
	 * ImageIO.write() returns false instead of throwing when it can't encode
	 * the image (e.g. writing an ARGB image - which has an alpha channel -
	 * as JPEG, which doesn't support transparency at all). JPEG output is
	 * therefore flattened onto an opaque RGB image first; the boolean result
	 * is also checked so a real failure surfaces as an exception instead of
	 * a misleading "written to" message for a file that doesn't exist.
	 */
	private void writeImage(BufferedImage image, String format, File file) throws IOException {
		BufferedImage toWrite = image;
		if ("jpg".equals(format)) {
			BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = rgb.createGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			g.drawImage(image, 0, 0, null);
			g.dispose();
			toWrite = rgb;
		}

		boolean written = ImageIO.write(toWrite, format, file);
		if (!written) {
			throw new IOException("No ImageIO writer available for format '" + format + "' (file: "
					+ file.getAbsolutePath() + ")");
		}
	}

	private String withSuffix(String imagePath, String suffix) {
		int dot = imagePath.lastIndexOf('.');
		if (dot < 0) {
			return imagePath + suffix + ".png";
		}
		return imagePath.substring(0, dot) + suffix + ".png";
	}

	private void tryOpen(File file) {
		try {
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
				Desktop.getDesktop().open(file);
			}
		}
		catch (Exception e) {
			System.out.println("(Could not auto-open the image - open it manually: " + file.getAbsolutePath() + ")");
		}
	}

}
