package com.findteam.findteam.notification;

import com.findteam.findteam.model.PreferredLanguage;

public record ApplicationRejectedNotification(
		Long authorTelegramId,
		PreferredLanguage recipientLanguage,
		Long applicantTelegramId,
		Long postId,
		Long applicationId,
		String postTitle) {}
