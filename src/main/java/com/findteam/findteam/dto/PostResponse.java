package com.findteam.findteam.dto;

import com.findteam.findteam.model.PostGoal;
import com.findteam.findteam.model.PostStatus;
import com.findteam.findteam.model.PostType;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PostResponse {

	private Long id;
	private Long telegramId;
	private String nickname;
	private PostType type;
	private String title;
	private String description;
	private String stack;
	private PostGoal goal;
	private PostStatus status;
	private String eventLink;
	private LocalDateTime createdAt;
}
