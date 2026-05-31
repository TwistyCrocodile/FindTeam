package com.findteam.findteam.exception;

public class ContactNotAvailableException extends RuntimeException {

	public ContactNotAvailableException(String message) {
		super(message);
	}

	public static ContactNotAvailableException notAccepted(Long applicationId) {
		return new ContactNotAvailableException(
				"Contact info is available only after application " + applicationId + " is accepted");
	}

	public static ContactNotAvailableException accessDenied(Long applicationId, Long requesterTelegramId) {
		return new ContactNotAvailableException(
				"User " + requesterTelegramId + " cannot view contact info for application " + applicationId);
	}
}

