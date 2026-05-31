package com.findteam.findteam.controller;

import com.findteam.findteam.dto.ApplicationResponse;
import com.findteam.findteam.dto.CreateCurrentUserApplicationRequest;
import com.findteam.findteam.dto.CreateApplicationRequest;
import com.findteam.findteam.dto.TelegramAuthUser;
import com.findteam.findteam.service.ApplicationService;
import com.findteam.findteam.service.CurrentTelegramUserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

	private final ApplicationService applicationService;
	private final CurrentTelegramUserService currentTelegramUserService;

	public ApplicationController(
			ApplicationService applicationService,
			CurrentTelegramUserService currentTelegramUserService) {
		this.applicationService = applicationService;
		this.currentTelegramUserService = currentTelegramUserService;
	}

	@PostMapping
	public ResponseEntity<ApplicationResponse> apply(@Valid @RequestBody CreateApplicationRequest request) {
		ApplicationResponse body = applicationService.applyToPost(request.getPostId(), request.getTelegramId());
		return ResponseEntity.status(HttpStatus.CREATED).body(body);
	}

	@PostMapping("/me")
	public ResponseEntity<ApplicationResponse> applyForCurrentUser(
			@RequestHeader(name = "X-Telegram-Init-Data", required = false) String initData,
			@Valid @RequestBody CreateCurrentUserApplicationRequest request) {
		TelegramAuthUser authUser = currentTelegramUserService.resolve(initData);
		ApplicationResponse body = applicationService.applyToPost(request.getPostId(), authUser.telegramId());
		return ResponseEntity.status(HttpStatus.CREATED).body(body);
	}

	/** Lists applications submitted by the given user (for feed "already applied" state). */
	@GetMapping
	public ResponseEntity<List<ApplicationResponse>> getByApplicant(@RequestParam Long telegramId) {
		return ResponseEntity.ok(applicationService.getApplicationsByApplicant(telegramId));
	}

	/** Lists applications submitted by the verified Telegram user. */
	@GetMapping("/me")
	public ResponseEntity<List<ApplicationResponse>> getCurrentUserApplications(
			@RequestHeader(name = "X-Telegram-Init-Data", required = false) String initData) {
		TelegramAuthUser authUser = currentTelegramUserService.resolve(initData);
		return ResponseEntity.ok(applicationService.getApplicationsByApplicant(authUser.telegramId()));
	}

	@PatchMapping("/{id}/accept")
	public ResponseEntity<ApplicationResponse> accept(
			@PathVariable Long id,
			@RequestParam Long telegramId) {
		return ResponseEntity.ok(applicationService.acceptApplication(id, telegramId));
	}

	@PatchMapping("/{id}/accept-secure")
	public ResponseEntity<ApplicationResponse> acceptSecure(
			@PathVariable Long id,
			@RequestHeader(name = "X-Telegram-Init-Data", required = false) String initData) {
		TelegramAuthUser authUser = currentTelegramUserService.resolve(initData);
		return ResponseEntity.ok(applicationService.acceptApplication(id, authUser.telegramId()));
	}

	@PatchMapping("/{id}/reject")
	public ResponseEntity<ApplicationResponse> reject(
			@PathVariable Long id,
			@RequestParam Long telegramId) {
		return ResponseEntity.ok(applicationService.rejectApplication(id, telegramId));
	}

	@PatchMapping("/{id}/reject-secure")
	public ResponseEntity<ApplicationResponse> rejectSecure(
			@PathVariable Long id,
			@RequestHeader(name = "X-Telegram-Init-Data", required = false) String initData) {
		TelegramAuthUser authUser = currentTelegramUserService.resolve(initData);
		return ResponseEntity.ok(applicationService.rejectApplication(id, authUser.telegramId()));
	}
}
