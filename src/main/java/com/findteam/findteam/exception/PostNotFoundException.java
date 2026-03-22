package com.findteam.findteam.exception;

public class PostNotFoundException extends RuntimeException {

	public PostNotFoundException(Long postId) {
		super("Post not found for id: " + postId);
	}
}
