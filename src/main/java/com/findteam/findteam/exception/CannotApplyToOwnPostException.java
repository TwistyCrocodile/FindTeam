package com.findteam.findteam.exception;

public class CannotApplyToOwnPostException extends RuntimeException {

	public CannotApplyToOwnPostException(Long postId) {
		super("Cannot apply to your own post: " + postId);
	}
}
