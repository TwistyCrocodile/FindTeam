package com.findteam.findteam.dto;

import com.findteam.findteam.model.PreferredLanguage;
import com.findteam.findteam.model.UserStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class UserProfileResponse {

	private Long telegramId;
	private PreferredLanguage preferredLanguage;
	private String nickname;
	private String bio;
	private String stack;
	private List<String> interestedStacks;
	private String githubUrl;
	private UserStatus status;
	private LocalDateTime createdAt;
}
