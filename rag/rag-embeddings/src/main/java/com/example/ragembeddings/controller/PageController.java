package com.example.ragembeddings.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import com.example.ragembeddings.service.QaVectorService;

@Controller
public class PageController {

	private final QaVectorService qaVectorService;

	public PageController(QaVectorService qaVectorService) {
		this.qaVectorService = qaVectorService;
	}

	@GetMapping("/")
	public Mono<String> index(Model model) {
		return qaVectorService.listHistory()
			.doOnNext(history -> model.addAttribute("history", history))
			.thenReturn("index");
	}

}
