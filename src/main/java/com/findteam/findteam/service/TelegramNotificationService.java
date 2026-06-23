package com.findteam.findteam.service;

import com.findteam.findteam.model.PreferredLanguage;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class TelegramNotificationService {

	private static final Logger log = LoggerFactory.getLogger(TelegramNotificationService.class);

	private final ObjectProvider<TelegramBot> telegramBotProvider;
	private final TelegramNotificationMessages telegramNotificationMessages;

	public TelegramNotificationService(
			ObjectProvider<TelegramBot> telegramBotProvider,
			TelegramNotificationMessages telegramNotificationMessages) {
		this.telegramBotProvider = telegramBotProvider;
		this.telegramNotificationMessages = telegramNotificationMessages;
	}

	public void notifyNewApplication(
			Long authorTelegramId,
			PreferredLanguage recipientLanguage,
			Long applicantTelegramId,
			Long postId,
			String postTitle,
			String applicantNickname,
			String applicantTelegramUsername,
			String applicantStack) {
		TelegramBot telegramBot = telegramBotProvider.getIfAvailable();
		if (telegramBot == null) {
			log.info(
					"Telegram new application notification skipped because bot is not configured: authorTelegramId={}, applicantTelegramId={}, postId={}",
					authorTelegramId,
					applicantTelegramId,
					postId);
			return;
		}

		String applicantName = resolveApplicantName(applicantNickname, applicantTelegramUsername);
		String message = telegramNotificationMessages.buildNewApplicationMessage(
				recipientLanguage,
				postTitle,
				applicantName,
				applicantStack);
		sendMessage(
				telegramBot,
				authorTelegramId,
				authorTelegramId,
				applicantTelegramId,
				postId,
				null,
				message,
				"new application");
	}

	public void notifyApplicationAccepted(
			Long authorTelegramId,
			PreferredLanguage recipientLanguage,
			Long applicantTelegramId,
			Long postId,
			Long applicationId,
			String postTitle,
			String authorNickname,
			String authorTelegramUsername) {
		TelegramBot telegramBot = telegramBotProvider.getIfAvailable();
		if (telegramBot == null) {
			log.info(
					"Telegram application accepted notification skipped because bot is not configured: authorTelegramId={}, applicantTelegramId={}, postId={}, applicationId={}",
					authorTelegramId,
					applicantTelegramId,
					postId,
					applicationId);
			return;
		}

		String authorName = resolveUserName(authorNickname, authorTelegramUsername);
		String message = telegramNotificationMessages.buildApplicationAcceptedMessage(
				recipientLanguage,
				postTitle,
				authorName);
		sendMessage(
				telegramBot,
				applicantTelegramId,
				authorTelegramId,
				applicantTelegramId,
				postId,
				applicationId,
				message,
				"application accepted");
	}

	public void notifyApplicationRejected(
			Long authorTelegramId,
			PreferredLanguage recipientLanguage,
			Long applicantTelegramId,
			Long postId,
			Long applicationId,
			String postTitle) {
		TelegramBot telegramBot = telegramBotProvider.getIfAvailable();
		if (telegramBot == null) {
			log.info(
					"Telegram application rejected notification skipped because bot is not configured: authorTelegramId={}, applicantTelegramId={}, postId={}, applicationId={}",
					authorTelegramId,
					applicantTelegramId,
					postId,
					applicationId);
			return;
		}

		String message = telegramNotificationMessages.buildApplicationRejectedMessage(recipientLanguage, postTitle);
		sendMessage(
				telegramBot,
				applicantTelegramId,
				authorTelegramId,
				applicantTelegramId,
				postId,
				applicationId,
				message,
				"application rejected");
	}

	private void sendMessage(
			TelegramBot telegramBot,
			Long recipientTelegramId,
			Long authorTelegramId,
			Long applicantTelegramId,
			Long postId,
			Long applicationId,
			String message,
			String notificationType) {
		try {
			SendMessage request = new SendMessage((Object) recipientTelegramId, message);
			BaseResponse response = telegramBot.execute(request);
			if (!response.isOk()) {
				log.warn(
						"Telegram {} notification failed: authorTelegramId={}, applicantTelegramId={}, postId={}, applicationId={}, errorCode={}, description={}",
						notificationType,
						authorTelegramId,
						applicantTelegramId,
						postId,
						applicationId,
						response.errorCode(),
						response.description());
				return;
			}
			log.info(
					"Telegram {} notification sent: authorTelegramId={}, applicantTelegramId={}, postId={}, applicationId={}",
					notificationType,
					authorTelegramId,
					applicantTelegramId,
					postId,
					applicationId);
		} catch (Exception ex) {
			log.warn(
					"Telegram {} notification failed: authorTelegramId={}, applicantTelegramId={}, postId={}, applicationId={}, exception={}, message={}",
					notificationType,
					authorTelegramId,
					applicantTelegramId,
					postId,
					applicationId,
					ex.getClass().getSimpleName(),
					ex.getMessage());
		}
	}

	private String resolveApplicantName(String nickname, String telegramUsername) {
		return resolveUserName(nickname, telegramUsername);
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
