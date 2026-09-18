package com.example.imageprocessing;

import java.nio.file.Path;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Usage:
 *   &lt;image-path&gt;
 *
 * Runs both steps in one call: detects the areas in the photo and writes
 * them as a JSON file, then draws those areas onto a new annotated image so
 * the prediction can be visually verified.
 */
@SpringBootApplication
public class OllamaVisionDetectionApplication {

	public static void main(String[] args) {
		SpringApplication.run(OllamaVisionDetectionApplication.class, args);
	}

	@Bean
	CommandLineRunner cli(AreaDetectionService detectionService, AreaVisualizationService visualizationService,
			ConfigurableApplicationContext context) {
		return args -> {
			try {
				if (args.length != 1) {
					printUsage();
					return;
				}

				String imagePath = args[0];

				// Step 1: image -> areas JSON
				Path jsonPath = detectionService.detectAndWrite(imagePath, null);

				// Step 2: image + areas JSON -> new annotated image
				visualizationService.visualize(imagePath, jsonPath.toString(), null);
			}
			finally {
				context.close();
			}
		};
	}

	private void printUsage() {
		System.out.println("""
				Usage:
				  <image-path>

				Example:
				  checkout.jpg
				""");
	}

}
