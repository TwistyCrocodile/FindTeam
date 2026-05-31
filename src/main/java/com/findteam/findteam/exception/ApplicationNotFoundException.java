package com.findteam.findteam.exception;

public class ApplicationNotFoundException extends RuntimeException {

	public ApplicationNotFoundException(Long applicationId) {
		super("Application not found for id: " + applicationId);
	}
}
