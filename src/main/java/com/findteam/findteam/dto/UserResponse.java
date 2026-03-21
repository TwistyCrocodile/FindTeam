package com.findteam.findteam.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserResponse {

	private Long id;
	private Long telegramId;
	private String username;
	private String bio;
	private String stack;
	private String githubUrl;
	private LocalDateTime createdAt;
}
