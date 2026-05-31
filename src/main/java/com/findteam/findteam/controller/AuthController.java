package com.findteam.findteam.controller;

import com.findteam.findteam.dto.TelegramAuthUser;
import com.findteam.findteam.service.TelegramInitDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final TelegramInitDataService telegramInitDataService;

	public AuthController(TelegramInitDataService telegramInitDataService) {
		this.telegramInitDataService = telegramInitDataService;
	}

	@GetMapping("/me")
	public ResponseEntity<TelegramAuthUser> getMe(
			@RequestHeader(name = "X-Telegram-Init-Data", required = false) String initData) {
		return ResponseEntity.ok(telegramInitDataService.verifyAndExtractUser(initData));
	}
}

