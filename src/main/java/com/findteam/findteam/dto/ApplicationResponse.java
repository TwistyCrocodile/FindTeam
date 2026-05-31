package com.findteam.findteam.dto;

import com.findteam.findteam.model.ApplicationStatus;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ApplicationResponse {

	private Long id;
	private Long postId;
	private String applicantNickname;
	private Long applicantTelegramId;
	private ApplicationStatus status;
	private boolean contactAvailable;
	private LocalDateTime createdAt;
}
