package com.example.java_ai_function_callback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

@SpringBootApplication
public class SpringAiJavaFunctionCallbackApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAiJavaFunctionCallbackApplication.class, args);
	}

	@Bean
	public CommandLineRunner init(ChatClient.Builder chatClientBuilder, ToolCallback weatherFunctionInfo,
			DateTools dateTools, @Value("classpath:SKILL.md") Resource skill) throws IOException {

		String skillInstructions = StreamUtils.copyToString(skill.getInputStream(), StandardCharsets.UTF_8);

		return args -> {
			try {
				ChatClient chatClient = chatClientBuilder
						.defaultSystem(skillInstructions)
						.build();
				ChatResponse response = chatClient
						.prompt("What are the weather conditions in San Francisco, Tokyo, and Paris? Find the temperature in Celsius for each of the three locations. Also tell me today's date.")
						.tools(weatherFunctionInfo, dateTools)
						.call().chatResponse();

				System.out.println("Response: " + response);
				System.out.println("Exiting successfully");
				System.exit(0);
			}
			catch (Exception e) {
				System.out.println("Error during weather check: " + e.getMessage());
			}
		};
	}

	public enum Unit {
		C("metric"),
		F("imperial");

		private final String unitName;

		Unit(String unitName) {
			this.unitName = unitName;
		}

		public String getUnitName() {
			return unitName;
		}
	}

	@Configuration
	static class Config {

		@Bean
		public ToolCallback weatherFunctionInfo(Function<WeatherRequest, WeatherResponse> currentWeather) {
			return FunctionToolCallback.builder("WeatherInfo", currentWeather)
					.description(
							"Find the weather conditions, forecasts, and temperatures for a location, like a city or state."
					)
					.inputType(WeatherRequest.class)
					.build();
		}

		@Bean
		public Function<WeatherRequest, WeatherResponse> currentWeather() {
			return request -> new MockJavaWeatherService().apply(request);
		}

		@Bean
		public DateTools dateTools() {
			return new DateTools();
		}

	}

	static class MockJavaWeatherService implements Function<WeatherRequest, WeatherResponse> {

		@Override
		public WeatherResponse apply(WeatherRequest weatherRequest) {
			double temperature = 10.0;
			if (weatherRequest.getLocation().contains("Paris")) {
				temperature = 15.0;
			}
			else if (weatherRequest.getLocation().contains("Tokyo")) {
				temperature = 10.0;
			}
			else if (weatherRequest.getLocation().contains("San Francisco")) {
				temperature = 30.0;
			}

			return new WeatherResponse(temperature, 15.0, 20.0, 2.0, 53, 45, Unit.C);
		}

	}

}
