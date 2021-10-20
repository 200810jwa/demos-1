package com.revature.controllers;

import java.util.List;
import java.util.Optional;

import com.revature.exceptions.BadRequestException;
import com.revature.intercom.FlashcardClient;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.revature.models.Flashcard;
import com.revature.models.Quiz;
import com.revature.repositories.QuizRepository;

@RestController
@RequestMapping("/quiz")
public class QuizController {

	private QuizRepository quizDao;
	private final FlashcardClient flashcardClient;
	
	public QuizController(FlashcardClient flashcardClient) {
		this.flashcardClient = flashcardClient;
	}
	
	@Autowired
	public void setQuizDao(QuizRepository dao) {
		this.quizDao = dao;
	}
	
	@GetMapping
	public ResponseEntity<List<Quiz>> findAll() {
		List<Quiz> all = quizDao.findAll();
		
		if(all.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		
		return ResponseEntity.ok(all);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Quiz> findById(@PathVariable("id") int id) {
		Optional<Quiz> optional = quizDao.findById(id);

//		if(optional.isPresent()) {
//			return ResponseEntity.ok(optional.get());
//		}
//
//		return ResponseEntity.noContent().build();

		// functional style! :)
		return optional.map(ResponseEntity::ok)
					   .orElseGet(() -> ResponseEntity.noContent().build());

	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Quiz insert(@RequestBody Quiz quiz) {
		int id = quiz.getId();
		
		if(id != 0) {
			throw new BadRequestException("Invalid id provided for new resource.");
		}
		
		quizDao.save(quiz);
		return quiz;
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/cards")
	public ResponseEntity<List<Flashcard>> getCards() {
		List<Flashcard> all = flashcardClient.findAll().getBody();
		
		if(all.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		
		return ResponseEntity.ok(all);
	}

	@ExceptionHandler
	@ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
	public void handleBadRequest(BadRequestException e) {}


	// Simple, yet effective, solution for handling failed Feign Client calls.
	// But...
	// We will look at the circuit-breaker design pattern
//	@ExceptionHandler
//	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
//	public void handleServiceUnavailable(FeignException e) {}

}
