package com.example.ragembeddings.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import com.example.ragembeddings.service.QaVectorService;

@Controller
public class QuestionController {

	private final QaVectorService qaVectorService;

	public QuestionController(QaVectorService qaVectorService) {
		this.qaVectorService = qaVectorService;
	}

	/** Live similarity search over past questions as the user types a new one. */
	@PostMapping("/api/suggestions")
	public Mono<String> suggestions(@RequestParam(required = false) String question, Model model) {
		return qaVectorService.suggest(question)
			.doOnNext(suggestions -> model.addAttribute("suggestions", suggestions))
			.thenReturn("fragments/suggestions :: list");
	}

	/** Submits a new question to the LLM, then embeds and persists the resulting Q&A pair. */
	@PostMapping("/api/questions")
	public Mono<String> ask(@RequestParam String question, Model model) {
		return qaVectorService.ask(question)
			.doOnNext(result -> model.addAttribute("result", result))
			.thenReturn("fragments/answer :: content")
			.onErrorResume(ex -> {
				model.addAttribute("error", ex.getMessage());
				return Mono.just("fragments/answer :: error");
			});
	}

	/** Loads a previously answered question into the answer pane - no LLM call. */
	@GetMapping("/api/questions/{id}")
	public Mono<String> loadQuestion(@PathVariable String id, Model model) {
		return qaVectorService.findById(id).map(found -> {
			if (found.isPresent()) {
				model.addAttribute("result", found.get());
				return "fragments/answer :: content";
			}
			model.addAttribute("error", "That question could not be found.");
			return "fragments/answer :: error";
		});
	}

	/** Renders the left-hand history panel. */
	@GetMapping("/history")
	public Mono<String> history(Model model) {
		return qaVectorService.listHistory()
			.doOnNext(history -> model.addAttribute("history", history))
			.thenReturn("fragments/history :: list");
	}

}
