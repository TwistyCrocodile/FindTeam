package com.findteam.findteam.dto;

import com.findteam.findteam.model.PreferredLanguage;
import com.findteam.findteam.model.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Data;

@Data
public class CreateUserProfileRequest {

	@NotNull(message = "telegramId must not be null")
	private Long telegramId;

	/**
	 * Public nickname (unique) shown inside the app.
	 * Allowed characters: letters, numbers, underscore, dot.
	 */
	@NotBlank(message = "nickname must not be blank")
	@Pattern(
			regexp = "^[A-Za-z0-9_.]{3,32}$",
			message = "nickname must be 3..32 characters and contain only letters, numbers, underscore, dot")
	private String nickname;

	/** Optional short description. */
	@Size(max = 1000, message = "bio must be at most 1000 characters")
	private String bio;

	/** Tech stack / skills summary. */
	@NotBlank(message = "stack must not be blank")
	@Size(max = 500, message = "stack must be at most 500 characters")
	private String stack;

	private List<@Size(max = 64, message = "interestedStacks item must be at most 64 characters") String> interestedStacks;

	/**
	 * Optional profile link. Empty or omitted is allowed; when set, must look like an http(s) URL.
	 * (Jakarta Bean Validation has no built-in {@code @URL}; a small pattern keeps this dependency-free of extra imports.)
	 */
	@Size(max = 255, message = "githubUrl must be at most 255 characters")
	@Pattern(
			regexp = "^(https?://\\S+|)$",
			message = "githubUrl must be a valid URL")
	private String githubUrl;

	private UserStatus status;

	private PreferredLanguage preferredLanguage;
}
