package com.example.ragembeddings;

import com.example.ragembeddings.config.RagProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RagProperties.class)
public class RagEmbeddingsApplication {

	public static void main(String[] args) {
		SpringApplication.run(RagEmbeddingsApplication.class, args);
	}

}
