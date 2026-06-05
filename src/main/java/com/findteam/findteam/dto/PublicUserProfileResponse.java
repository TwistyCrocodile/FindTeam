package com.findteam.findteam.dto;

import com.findteam.findteam.model.UserStatus;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Public profile fields only — no contact unlock fields.
 */
@Data
public class PublicUserProfileResponse {

	private Long telegramId;
	private String nickname;
	private String bio;
	private String stack;
	private String githubUrl;
	private UserStatus status;
	private LocalDateTime createdAt;
}
