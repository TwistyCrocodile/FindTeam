package com.findteam.findteam.controller;

import com.findteam.findteam.dto.ContactInfoResponse;
import com.findteam.findteam.dto.CreateUserProfileRequest;
import com.findteam.findteam.dto.RegisterCurrentUserRequest;
import com.findteam.findteam.dto.PostResponse;
import com.findteam.findteam.dto.PublicUserProfileResponse;
import com.findteam.findteam.dto.RegisterUserRequest;
import com.findteam.findteam.dto.TelegramAuthUser;
import com.findteam.findteam.dto.UpdateContactInfoRequest;
import com.findteam.findteam.dto.UpdateUserProfileRequest;
import com.findteam.findteam.dto.UserProfileResponse;
import com.findteam.findteam.service.CurrentTelegramUserService;
import com.findteam.findteam.service.PostService;
import com.findteam.findteam.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
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
	private final PostService postService;
	private final CurrentTelegramUserService currentTelegramUserService;

	public UserController(
			UserService userService,
			PostService postService,
			CurrentTelegramUserService currentTelegramUserService) {
		this.userService = userService;
		this.postService = postService;
		this.currentTelegramUserService = currentTelegramUserService;
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
		TelegramAuthUser authUser = currentTelegramUserService.resolve(initData);
		UserProfileResponse body = userService.registerCurrentUser(authUser, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(body);
	}

	@GetMapping("/{telegramId}")
	public ResponseEntity<UserProfileResponse> getByTelegramId(@PathVariable Long telegramId) {
		return ResponseEntity.ok(userService.getByTelegramId(telegramId));
	}

	@GetMapping("/{telegramId}/public-profile")
	public ResponseEntity<PublicUserProfileResponse> getPublicProfile(@PathVariable Long telegramId) {
		return ResponseEntity.ok(userService.getPublicProfileByTelegramId(telegramId));
	}

	@GetMapping("/{telegramId}/public-posts")
	public ResponseEntity<List<PostResponse>> getPublicPosts(@PathVariable Long telegramId) {
		return ResponseEntity.ok(postService.getPublicPostsByTelegramId(telegramId));
	}

	@GetMapping("/by-telegram/{telegramId}")
	public ResponseEntity<UserProfileResponse> getByTelegramIdForOnboarding(@PathVariable Long telegramId) {
		return ResponseEntity.ok(userService.getByTelegramId(telegramId));
	}

	@GetMapping("/me")
	public ResponseEntity<UserProfileResponse> getCurrentUserProfile(
			@RequestHeader(name = "X-Telegram-Init-Data", required = false) String initData) {
		TelegramAuthUser authUser = currentTelegramUserService.resolve(initData);
		return ResponseEntity.ok(userService.getByTelegramId(authUser.telegramId()));
	}

	@GetMapping("/me/contact")
	public ResponseEntity<ContactInfoResponse> getCurrentUserContactInfo(
			@RequestHeader(name = "X-Telegram-Init-Data", required = false) String initData) {
		TelegramAuthUser authUser = currentTelegramUserService.resolve(initData);
		return ResponseEntity.ok(userService.getContactInfo(authUser.telegramId()));
	}

	@PutMapping("/me/contact")
	public ResponseEntity<ContactInfoResponse> updateCurrentUserContactInfo(
			@RequestHeader(name = "X-Telegram-Init-Data", required = false) String initData,
			@Valid @RequestBody UpdateContactInfoRequest request) {
		TelegramAuthUser authUser = currentTelegramUserService.resolve(initData);
		return ResponseEntity.ok(userService.updateContactInfo(authUser.telegramId(), request));
	}

	@PutMapping("/me")
	public ResponseEntity<UserProfileResponse> updateCurrentUserProfile(
			@RequestHeader(name = "X-Telegram-Init-Data", required = false) String initData,
			@Valid @RequestBody UpdateUserProfileRequest request) {
		TelegramAuthUser authUser = currentTelegramUserService.resolve(initData);
		return ResponseEntity.ok(userService.updateUserProfile(authUser.telegramId(), request));
	}

	@PutMapping("/{telegramId}")
	public ResponseEntity<UserProfileResponse> updateUserProfile(
			@PathVariable Long telegramId,
			@Valid @RequestBody UpdateUserProfileRequest request) {
		return ResponseEntity.ok(userService.updateUserProfile(telegramId, request));
	}
}
