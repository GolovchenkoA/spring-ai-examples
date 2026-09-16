package com.example.java_ai_function_callback;

import java.time.LocalDate;

import org.springframework.ai.tool.annotation.Tool;

public class DateTools {

	@Tool(description = "Returns the current date")
	public String getCurrentDate() {
		return LocalDate.now().toString();
	}

}
