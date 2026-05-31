package com.findteam.findteam.exception;

public class ApplicationAlreadyExistsException extends RuntimeException {

	public ApplicationAlreadyExistsException(Long postId, Long applicantTelegramId) {
		super("Application already exists for postId=" + postId + ", applicantTelegramId=" + applicantTelegramId);
	}
}
