package com.findteam.findteam.notification;

import com.findteam.findteam.model.PreferredLanguage;

public record ApplicationAcceptedNotification(
		Long authorTelegramId,
		PreferredLanguage recipientLanguage,
		Long applicantTelegramId,
		Long postId,
		Long applicationId,
		String postTitle,
		String authorNickname,
		String authorTelegramUsername) {}
