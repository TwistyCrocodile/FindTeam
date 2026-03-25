package com.findteam.findteam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserProfileRequest {

	@NotBlank(message = "nickname must not be blank")
	@Pattern(
			regexp = "^[A-Za-z0-9_.]{3,32}$",
			message = "nickname must be 3..32 characters and contain only letters, numbers, underscore, dot")
	private String nickname;

	/** Optional short description. */
	@Size(max = 500, message = "bio must be at most 500 characters")
	private String bio;

	/** Keep consistent with create: stack required. */
	@NotBlank(message = "stack must not be blank")
	@Size(max = 255, message = "stack must be at most 255 characters")
	private String stack;

	@Size(max = 255, message = "githubUrl must be at most 255 characters")
	@Pattern(
			regexp = "^(https?://\\S+|)$",
			message = "githubUrl must be a valid URL")
	private String githubUrl;
}

