package com.example.ragembeddings.config;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {

	private static final Log logger = LogFactory.getLog(VectorStoreConfig.class);

	@Bean
	public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
		return chatClientBuilder.build();
	}

	@Bean
	public SimpleVectorStore vectorStore(EmbeddingModel embeddingModel, RagProperties ragProperties) {
		SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

		File storeFile = new File(ragProperties.storePath());
		if (storeFile.exists()) {
			logger.info("Loading persisted vector store from " + storeFile.getAbsolutePath());
			vectorStore.load(storeFile);
		}
		else {
			File parent = storeFile.getAbsoluteFile().getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}
			logger.info("No persisted vector store found at " + storeFile.getAbsolutePath() + " - starting empty");
		}
		return vectorStore;
	}

}
