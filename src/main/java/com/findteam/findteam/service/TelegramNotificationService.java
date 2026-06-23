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
		sendMessage(telegramBot, authorTelegramId, applicantTelegramId, postId, message, "new application");
	}

	private void sendMessage(
			TelegramBot telegramBot,
			Long authorTelegramId,
			Long applicantTelegramId,
			Long postId,
			String message,
			String notificationType) {
		try {
			SendMessage request = new SendMessage((Object) authorTelegramId, message);
			BaseResponse response = telegramBot.execute(request);
			if (!response.isOk()) {
				log.warn(
						"Telegram {} notification failed: authorTelegramId={}, applicantTelegramId={}, postId={}, errorCode={}, description={}",
						notificationType,
						authorTelegramId,
						applicantTelegramId,
						postId,
						response.errorCode(),
						response.description());
				return;
			}
			log.info(
					"Telegram {} notification sent: authorTelegramId={}, applicantTelegramId={}, postId={}",
					notificationType,
					authorTelegramId,
					applicantTelegramId,
					postId);
		} catch (Exception ex) {
			log.warn(
					"Telegram {} notification failed: authorTelegramId={}, applicantTelegramId={}, postId={}, exception={}, message={}",
					notificationType,
					authorTelegramId,
					applicantTelegramId,
					postId,
					ex.getClass().getSimpleName(),
					ex.getMessage());
		}
	}

	private String resolveApplicantName(String nickname, String telegramUsername) {
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
