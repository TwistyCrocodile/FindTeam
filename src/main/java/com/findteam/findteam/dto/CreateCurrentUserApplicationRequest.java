package com.findteam.findteam.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCurrentUserApplicationRequest {

	@NotNull(message = "postId must not be null")
	private Long postId;
}

