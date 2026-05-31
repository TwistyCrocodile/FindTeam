package com.findteam.findteam.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateContactInfoRequest {

	@Size(max = 32, message = "contactTelegramUsername must be at most 32 characters")
	@Pattern(
			regexp = "^([A-Za-z0-9_]{5,32}|)$",
			message = "contactTelegramUsername must be 5..32 characters and contain only letters, numbers, underscore")
	private String contactTelegramUsername;

	@Size(max = 255, message = "contactGithubUrl must be at most 255 characters")
	@Pattern(
			regexp = "^(https?://\\S+|)$",
			message = "contactGithubUrl must be a valid URL")
	private String contactGithubUrl;

	@Size(max = 255, message = "contactEmail must be at most 255 characters")
	@Email(message = "contactEmail must be a valid email")
	private String contactEmail;
}

