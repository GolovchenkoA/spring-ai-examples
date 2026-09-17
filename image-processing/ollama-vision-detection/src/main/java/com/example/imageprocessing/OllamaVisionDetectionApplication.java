package com.example.imageprocessing;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Usage:
 *   detect    &lt;image-path&gt; [output-json-path]
 *   visualize &lt;image-path&gt; &lt;areas-json-path&gt; [output-image-path]
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
				if (args.length < 1) {
					printUsage();
					return;
				}

				String mode = args[0];
				switch (mode) {
					case "detect" -> detectionService.detectAndWrite(args[1], args.length > 2 ? args[2] : null);
					case "visualize" -> {
						if (args.length < 3) {
							printUsage();
							return;
						}
						visualizationService.visualize(args[1], args[2], args.length > 3 ? args[3] : null);
					}
					default -> printUsage();
				}
			}
			finally {
				context.close();
			}
		};
	}

	private void printUsage() {
		System.out.println("""
				Usage:
				  detect    <image-path> [output-json-path]
				  visualize <image-path> <areas-json-path> [output-image-path]

				Examples:
				  detect    checkout.jpg
				  detect    checkout.jpg checkout.areas.json
				  visualize checkout.jpg checkout.jpg.areas.json
				""");
	}

}
