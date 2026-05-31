package com.findteam.findteam.controller;

import com.findteam.findteam.dto.CreateUserProfileRequest;
import com.findteam.findteam.dto.RegisterCurrentUserRequest;
import com.findteam.findteam.dto.RegisterUserRequest;
import com.findteam.findteam.dto.TelegramAuthUser;
import com.findteam.findteam.dto.UpdateUserProfileRequest;
import com.findteam.findteam.dto.UserProfileResponse;
import com.findteam.findteam.service.TelegramInitDataService;
import com.findteam.findteam.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;
	private final TelegramInitDataService telegramInitDataService;

	public UserController(UserService userService, TelegramInitDataService telegramInitDataService) {
		this.userService = userService;
		this.telegramInitDataService = telegramInitDataService;
	}

	@PostMapping
	public ResponseEntity<UserProfileResponse> createUserProfile(@Valid @RequestBody CreateUserProfileRequest request) {
		UserProfileResponse body = userService.createUserProfile(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(body);
	}

	@PostMapping("/register")
	public ResponseEntity<UserProfileResponse> registerUser(@Valid @RequestBody RegisterUserRequest request) {
		UserProfileResponse body = userService.registerUser(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(body);
	}

	@PostMapping("/me/register")
	public ResponseEntity<UserProfileResponse> registerCurrentUser(
			@RequestHeader(name = "X-Telegram-Init-Data", required = false) String initData,
			@Valid @RequestBody RegisterCurrentUserRequest request) {
		TelegramAuthUser authUser = telegramInitDataService.verifyAndExtractUser(initData);
		UserProfileResponse body = userService.registerCurrentUser(authUser.telegramId(), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(body);
	}

	@GetMapping("/{telegramId}")
	public ResponseEntity<UserProfileResponse> getByTelegramId(@PathVariable Long telegramId) {
		return ResponseEntity.ok(userService.getByTelegramId(telegramId));
	}

	@GetMapping("/by-telegram/{telegramId}")
	public ResponseEntity<UserProfileResponse> getByTelegramIdForOnboarding(@PathVariable Long telegramId) {
		return ResponseEntity.ok(userService.getByTelegramId(telegramId));
	}

	@GetMapping("/me")
	public ResponseEntity<UserProfileResponse> getCurrentUserProfile(
			@RequestHeader(name = "X-Telegram-Init-Data", required = false) String initData) {
		TelegramAuthUser authUser = telegramInitDataService.verifyAndExtractUser(initData);
		return ResponseEntity.ok(userService.getByTelegramId(authUser.telegramId()));
	}

	@PutMapping("/{telegramId}")
	public ResponseEntity<UserProfileResponse> updateUserProfile(
			@PathVariable Long telegramId,
			@Valid @RequestBody UpdateUserProfileRequest request) {
		return ResponseEntity.ok(userService.updateUserProfile(telegramId, request));
	}
}
