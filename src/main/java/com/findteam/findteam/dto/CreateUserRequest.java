package com.findteam.findteam.dto;

import lombok.Data;

@Data
public class CreateUserRequest {

	private Long telegramId;
	private String username;
	private String bio;
	private String stack;
	private String githubUrl;
}
