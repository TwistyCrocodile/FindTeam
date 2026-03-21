package com.findteam.findteam.exception;

public class UserAlreadyExistsException extends RuntimeException {

	public UserAlreadyExistsException(Long telegramId) {
		super("User already exists for telegramId: " + telegramId);
	}
}
