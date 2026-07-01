package com.findteam.findteam.service;

import com.findteam.findteam.notification.ApplicationAcceptedNotification;
import com.findteam.findteam.notification.ApplicationRejectedNotification;
import com.findteam.findteam.notification.MatchingPostNotification;
import com.findteam.findteam.notification.NewApplicationNotification;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

	private final TelegramNotificationService telegramNotificationService;
	private final TelegramNotificationMessages telegramNotificationMessages;

	public NotificationService(
			TelegramNotificationService telegramNotificationService,
			TelegramNotificationMessages telegramNotificationMessages) {
		this.telegramNotificationService = telegramNotificationService;
		this.telegramNotificationMessages = telegramNotificationMessages;
	}

	public void notifyNewApplication(NewApplicationNotification notification) {
		String applicantName = resolveUserName(notification.applicantNickname(), notification.applicantTelegramUsername());
		String message = telegramNotificationMessages.buildNewApplicationMessage(
				notification.recipientLanguage(),
				notification.postTitle(),
				applicantName,
				notification.applicantStack());
		telegramNotificationService.sendMessage(
				notification.authorTelegramId(),
				message,
				"new application",
				notification.authorTelegramId(),
				notification.applicantTelegramId(),
				notification.postId(),
				null);
	}

	public void notifyApplicationAccepted(ApplicationAcceptedNotification notification) {
		String authorName = resolveUserName(notification.authorNickname(), notification.authorTelegramUsername());
		String message = telegramNotificationMessages.buildApplicationAcceptedMessage(
				notification.recipientLanguage(),
				notification.postTitle(),
				authorName);
		telegramNotificationService.sendMessage(
				notification.applicantTelegramId(),
				message,
				"application accepted",
				notification.authorTelegramId(),
				notification.applicantTelegramId(),
				notification.postId(),
				notification.applicationId());
	}

	public void notifyApplicationRejected(ApplicationRejectedNotification notification) {
		String message = telegramNotificationMessages.buildApplicationRejectedMessage(
				notification.recipientLanguage(),
				notification.postTitle());
		telegramNotificationService.sendMessage(
				notification.applicantTelegramId(),
				message,
				"application rejected",
				notification.authorTelegramId(),
				notification.applicantTelegramId(),
				notification.postId(),
				notification.applicationId());
	}

	public void notifyMatchingPost(MatchingPostNotification notification) {
		String message = telegramNotificationMessages.buildNewMatchingPostMessage(
				notification.recipientLanguage(),
				notification.postTitle(),
				notification.matchedTechnologies());
		telegramNotificationService.sendMessage(
				notification.recipientTelegramId(),
				message,
				"matching post",
				notification.authorTelegramId(),
				notification.recipientTelegramId(),
				notification.postId(),
				null);
	}

	private String resolveUserName(String nickname, String telegramUsername) {
		if (hasText(nickname)) {
			return nickname.trim();
		}
		if (hasText(telegramUsername)) {
			String normalized = telegramUsername.trim();
			return normalized.startsWith("@") ? normalized : "@" + normalized;
		}
		return "";
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}
}
