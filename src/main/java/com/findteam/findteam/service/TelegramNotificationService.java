package com.findteam.findteam.service;

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

	public TelegramNotificationService(ObjectProvider<TelegramBot> telegramBotProvider) {
		this.telegramBotProvider = telegramBotProvider;
	}

	public void sendMessage(
			Long recipientTelegramId,
			String message,
			String notificationType,
			Long authorTelegramId,
			Long applicantTelegramId,
			Long postId,
			Long applicationId) {
		TelegramBot telegramBot = telegramBotProvider.getIfAvailable();
		if (telegramBot == null) {
			log.info(
					"Telegram {} notification skipped because bot is not configured: authorTelegramId={}, applicantTelegramId={}, postId={}, applicationId={}",
					notificationType,
					authorTelegramId,
					applicantTelegramId,
					postId,
					applicationId);
			return;
		}

		sendMessage(
				telegramBot,
				recipientTelegramId,
				authorTelegramId,
				applicantTelegramId,
				postId,
				applicationId,
				message,
				notificationType);
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

}
