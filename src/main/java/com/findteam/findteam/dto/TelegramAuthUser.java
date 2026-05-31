package com.findteam.findteam.dto;

public record TelegramAuthUser(
		Long telegramId,
		String username,
		String firstName,
		String lastName) {}

