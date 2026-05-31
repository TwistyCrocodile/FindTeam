package com.findteam.findteam.dto;

import com.findteam.findteam.model.ApplicationStatus;
import com.findteam.findteam.model.PostGoal;
import com.findteam.findteam.model.PostStatus;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ApplicationResponse {

	private Long id;
	private Long postId;
	private String postTitle;
	private PostGoal postGoal;
	private PostStatus postStatus;
	private String applicantNickname;
	private Long applicantTelegramId;
	private ApplicationStatus status;
	private boolean contactAvailable;
	private LocalDateTime createdAt;
}
