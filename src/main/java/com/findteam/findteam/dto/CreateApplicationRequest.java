package com.findteam.findteam.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateApplicationRequest {

	@NotNull(message = "postId must not be null")
	private Long postId;

	@NotNull(message = "telegramId must not be null")
	private Long telegramId;
}
