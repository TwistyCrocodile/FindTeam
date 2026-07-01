package com.findteam.findteam.notification;

import com.findteam.findteam.model.PreferredLanguage;

public record NewApplicationNotification(
		Long authorTelegramId,
		PreferredLanguage recipientLanguage,
		Long applicantTelegramId,
		Long postId,
		String postTitle,
		String applicantNickname,
		String applicantTelegramUsername,
		String applicantStack) {}
