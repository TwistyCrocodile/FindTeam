package com.findteam.findteam.controller;

import com.findteam.findteam.dto.ApplicationResponse;
import com.findteam.findteam.dto.CreateApplicationRequest;
import com.findteam.findteam.service.ApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

	private final ApplicationService applicationService;

	public ApplicationController(ApplicationService applicationService) {
		this.applicationService = applicationService;
	}

	@PostMapping
	public ResponseEntity<ApplicationResponse> apply(@Valid @RequestBody CreateApplicationRequest request) {
		ApplicationResponse body = applicationService.applyToPost(request.getPostId(), request.getTelegramId());
		return ResponseEntity.status(HttpStatus.CREATED).body(body);
	}

	/** Lists applications submitted by the given user (for feed "already applied" state). */
	@GetMapping
	public ResponseEntity<List<ApplicationResponse>> getByApplicant(@RequestParam Long telegramId) {
		return ResponseEntity.ok(applicationService.getApplicationsByApplicant(telegramId));
	}

	@PatchMapping("/{id}/accept")
	public ResponseEntity<ApplicationResponse> accept(
			@PathVariable Long id,
			@RequestParam Long telegramId) {
		return ResponseEntity.ok(applicationService.acceptApplication(id, telegramId));
	}

	@PatchMapping("/{id}/reject")
	public ResponseEntity<ApplicationResponse> reject(
			@PathVariable Long id,
			@RequestParam Long telegramId) {
		return ResponseEntity.ok(applicationService.rejectApplication(id, telegramId));
	}
}
