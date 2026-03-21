package com.findteam.findteam.exception;

public class UserNotFoundException extends RuntimeException {

	public UserNotFoundException(Long telegramId) {
		super("User not found for telegramId: " + telegramId);
	}
}
