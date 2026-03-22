package com.findteam.findteam.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {

	@NotNull(message = "telegramId must not be null")
	private Long telegramId;

	/** Display name shown in the app. */
	@NotBlank(message = "username must not be blank")
	@Size(min = 3, max = 32, message = "username must be between 3 and 32 characters")
	private String username;

	/** Optional short description. */
	@Size(max = 500, message = "bio must be at most 500 characters")
	private String bio;

	/** Tech stack / skills summary. */
	@NotBlank(message = "stack must not be blank")
	@Size(max = 255, message = "stack must be at most 255 characters")
	private String stack;

	/**
	 * Optional profile link. Empty or omitted is allowed; when set, must look like an http(s) URL.
	 * (Jakarta Bean Validation has no built-in {@code @URL}; a small pattern keeps this dependency-free of extra imports.)
	 */
	@Size(max = 255, message = "githubUrl must be at most 255 characters")
	@Pattern(
			regexp = "^(https?://\\S+|)$",
			message = "githubUrl must be a valid URL")
	private String githubUrl;
}
