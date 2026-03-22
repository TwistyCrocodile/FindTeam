package com.findteam.findteam.dto;

import com.findteam.findteam.model.PostGoal;
import com.findteam.findteam.model.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePostRequest {

	@NotNull(message = "telegramId must not be null")
	private Long telegramId;

	@NotNull(message = "type must not be null")
	private PostType type;

	@NotBlank(message = "title must not be blank")
	@Size(max = 100, message = "title must be at most 100 characters")
	private String title;

	@NotBlank(message = "description must not be blank")
	@Size(max = 1000, message = "description must be at most 1000 characters")
	private String description;

	@NotBlank(message = "stack must not be blank")
	@Size(max = 255, message = "stack must be at most 255 characters")
	private String stack;

	@NotNull(message = "goal must not be null")
	private PostGoal goal;

	@Size(max = 255, message = "eventLink must be at most 255 characters")
	@Pattern(
			regexp = "^(https?://\\S+|)$",
			message = "eventLink must be a valid URL")
	private String eventLink;
}
