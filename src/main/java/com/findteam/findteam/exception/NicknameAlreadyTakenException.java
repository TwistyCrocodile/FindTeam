package com.findteam.findteam.exception;

public class NicknameAlreadyTakenException extends RuntimeException {

	public NicknameAlreadyTakenException(String nickname) {
		super("Nickname already taken: " + nickname);
	}
}

