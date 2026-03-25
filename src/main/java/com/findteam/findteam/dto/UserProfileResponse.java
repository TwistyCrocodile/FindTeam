package com.findteam.findteam.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserProfileResponse {

	private Long telegramId;
	private String nickname;
	private String bio;
	private String stack;
	private String githubUrl;
	private LocalDateTime createdAt;
}

