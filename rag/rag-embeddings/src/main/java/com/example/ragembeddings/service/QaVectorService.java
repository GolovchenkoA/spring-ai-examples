package com.example.ragembeddings.service;

import java.io.File;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import jakarta.annotation.PostConstruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.example.ragembeddings.config.RagProperties;
import com.example.ragembeddings.model.QaItem;
import com.example.ragembeddings.model.SuggestionItem;

/**
 * Core RAG playground logic: answers new questions with the LLM, embeds and persists them,
 * and live-searches past questions by embedding similarity.
 *
 * <p>Q&amp;A metadata (id, full question, answer, timestamp) is kept in an in-memory cache
 * mirroring the vector store, so listing history or loading a past answer never needs an
 * embedding-model call - only the live similarity search in {@link #suggest(String)} does.
 */
@Service
public class QaVectorService {

	private static final Log logger = LogFactory.getLog(QaVectorService.class);

	private static final String META_QA_ID = "qaId";
	private static final String META_FULL_QUESTION = "fullQuestion";
	private static final String META_ANSWER = "answer";
	private static final String META_CREATED_AT = "createdAt";

	private final ChatClient chatClient;
	private final SimpleVectorStore vectorStore;
	private final RagProperties ragProperties;
	private final ReentrantLock persistLock = new ReentrantLock();
	private final Map<String, QaItem> cache = new ConcurrentHashMap<>();

	public QaVectorService(ChatClient chatClient, SimpleVectorStore vectorStore, RagProperties ragProperties) {
		this.chatClient = chatClient;
		this.vectorStore = vectorStore;
		this.ragProperties = ragProperties;
	}

	/**
	 * Hydrates the in-memory cache from whatever was just loaded into the vector store at
	 * startup (a single, one-off similarity search over the whole store).
	 */
	@PostConstruct
	void hydrateCacheFromVectorStore() {
		if (!new File(ragProperties.storePath()).exists()) {
			return;
		}
		SearchRequest request = SearchRequest.builder()
			.query("*")
			.topK(Integer.MAX_VALUE)
			.similarityThresholdAll()
			.build();

		List<Document> documents = vectorStore.similaritySearch(request);
		for (Document doc : documents) {
			QaItem item = toQaItem(doc);
			cache.put(item.id(), item);
		}
		logger.info("Hydrated " + cache.size() + " past question(s) from " + ragProperties.storePath());
	}

	/** Sends the question to the LLM, stores the Q&A pair with its embedded title, and persists to disk. */
	public Mono<QaItem> ask(String fullQuestion) {
		return Mono.fromCallable(() -> doAsk(fullQuestion)).subscribeOn(Schedulers.boundedElastic());
	}

	/** Embeds the partial text and returns similar past questions above the configured threshold. */
	public Mono<List<SuggestionItem>> suggest(String partialText) {
		return Mono.fromCallable(() -> doSuggest(partialText)).subscribeOn(Schedulers.boundedElastic());
	}

	/** Loads a previously stored question/answer pair by id, without calling the LLM. */
	public Mono<Optional<QaItem>> findById(String id) {
		return Mono.just(Optional.ofNullable(cache.get(id)));
	}

	/** All stored questions, newest first, for the history panel. */
	public Mono<List<QaItem>> listHistory() {
		return Mono.fromSupplier(() -> cache.values().stream()
			.sorted(Comparator.comparing(QaItem::createdAt).reversed())
			.toList());
	}

	private QaItem doAsk(String fullQuestion) {
		String answer = chatClient.prompt().user(fullQuestion).call().content();

		String id = UUID.randomUUID().toString();
		String title = truncateTitle(fullQuestion);
		Instant createdAt = Instant.now();

		Document document = Document.builder()
			.id(id)
			.text(title)
			.metadata(Map.of(
					META_QA_ID, id,
					META_FULL_QUESTION, fullQuestion,
					META_ANSWER, answer,
					META_CREATED_AT, createdAt.toString()))
			.build();

		vectorStore.add(List.of(document));
		persist();

		QaItem item = new QaItem(id, title, fullQuestion, answer, createdAt);
		cache.put(id, item);
		return item;
	}

	private List<SuggestionItem> doSuggest(String partialText) {
		if (partialText == null || partialText.trim().length() < ragProperties.minSuggestionLength()) {
			return List.of();
		}

		SearchRequest request = SearchRequest.builder()
			.query(partialText)
			.topK(ragProperties.suggestionTopK())
			.similarityThreshold(ragProperties.similarityThreshold())
			.build();

		return vectorStore.similaritySearch(request).stream()
			.map(doc -> new SuggestionItem(doc.getId(), doc.getText(), scoreOf(doc)))
			.toList();
	}

	private QaItem toQaItem(Document doc) {
		Map<String, Object> metadata = doc.getMetadata();
		String id = (String) metadata.getOrDefault(META_QA_ID, doc.getId());
		String fullQuestion = (String) metadata.get(META_FULL_QUESTION);
		String answer = (String) metadata.get(META_ANSWER);
		Instant createdAt = Instant.parse((String) metadata.get(META_CREATED_AT));
		return new QaItem(id, doc.getText(), fullQuestion, answer, createdAt);
	}

	private double scoreOf(Document doc) {
		return doc.getScore() != null ? doc.getScore() : 0.0;
	}

	private String truncateTitle(String fullQuestion) {
		int n = ragProperties.titleLength();
		String trimmed = fullQuestion.trim();
		return trimmed.length() <= n ? trimmed : trimmed.substring(0, n) + "…";
	}

	private void persist() {
		persistLock.lock();
		try {
			File storeFile = new File(ragProperties.storePath());
			File parent = storeFile.getAbsoluteFile().getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
			vectorStore.save(storeFile);
		}
		catch (Exception ex) {
			logger.error("Failed to persist vector store to " + ragProperties.storePath(), ex);
		}
		finally {
			persistLock.unlock();
		}
	}

}
