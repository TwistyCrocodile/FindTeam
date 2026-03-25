package com.findteam.findteam.exception;

public class PostAccessDeniedException extends RuntimeException {

	public PostAccessDeniedException(Long postId, Long requesterTelegramId) {
		super("Access denied for postId=" + postId + ", requesterTelegramId=" + requesterTelegramId);
	}
}

