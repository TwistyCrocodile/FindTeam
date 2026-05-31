package com.findteam.findteam.exception;

public class CannotApplyToClosedPostException extends RuntimeException {

	public CannotApplyToClosedPostException(Long postId) {
		super("Cannot apply to a closed post: " + postId);
	}
}

