package com.findteam.findteam.notification;

import com.findteam.findteam.model.PreferredLanguage;
import java.util.List;

public record MatchingPostNotification(
		Long recipientTelegramId,
		PreferredLanguage recipientLanguage,
		Long authorTelegramId,
		Long postId,
		String postTitle,
		List<String> matchedTechnologies) {}
