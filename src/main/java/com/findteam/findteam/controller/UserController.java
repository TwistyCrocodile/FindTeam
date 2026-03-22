package com.findteam.findteam.controller;

import com.findteam.findteam.dto.CreateUserRequest;
import com.findteam.findteam.dto.UserResponse;
import com.findteam.findteam.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping
	public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
		UserResponse body = userService.createUser(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(body);
	}

	@GetMapping("/{telegramId}")
	public ResponseEntity<UserResponse> getByTelegramId(@PathVariable Long telegramId) {
		return ResponseEntity.ok(userService.getByTelegramId(telegramId));
	}
}
