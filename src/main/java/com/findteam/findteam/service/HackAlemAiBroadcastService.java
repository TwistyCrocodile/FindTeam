package com.findteam.findteam.service;

import com.findteam.findteam.model.MaintenanceTaskExecution;
import com.findteam.findteam.repository.MaintenanceTaskExecutionRepository;
import com.findteam.findteam.repository.UserRepository;
import com.pengrad.telegrambot.model.WebAppInfo;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HackAlemAiBroadcastService {

	public static final String TASK_KEY = "hack-alem-ai-2026-09";

	private static final Logger log = LoggerFactory.getLogger(HackAlemAiBroadcastService.class);

	private static final String MESSAGE = """
			🤖 <b>Hack Alem AI — крупнейший agentic-AI хакатон в мире с призами $1 000 000</b>

			23 сентября в Астане пройдёт офлайн-хакатон от OpenAI и Astana Hub. Участники соберут команды, построят AI-решение с использованием Codex и разделят $1 млн в призах и технологических ресурсах OpenAI.

			📍 Астана, офлайн (обязательное присутствие)
			📅 23 сентября 2026
			🗣 Формат: agentic AI, командная разработка
			💰 Призовой фонд и ресурсы: $1 000 000 (API-токены OpenAI, доступ к Codex, денежные призы)
			📝 Регистрация до 19 сентября 2026
			👥 Мест ограничено: 2 500, first come first served

			Участвовать могут студенты, молодые специалисты в IT и все, у кого уже есть опыт или образование в разработке. Обязательное условие — использование Codex в процессе работы над проектом. Возраст участников: 18+.

			Подойдут разработчики, дизайнеры, продакт-менеджеры и все, кто хочет создать AI-продукт и побороться за призовой фонд от OpenAI.

			🔗 Подробнее и регистрация:
			https://edu.astanahub.com/hackathons/df4743f5-c492-415c-b45a-1f13adb78e06

			Если ищете людей для участия — создавайте пост в FindTeam и собирайте команду заранее.
			""";

	private final UserRepository userRepository;
	private final MaintenanceTaskExecutionRepository maintenanceTaskExecutionRepository;
	private final TelegramNotificationService telegramNotificationService;
	private final String miniAppUrl;

	public HackAlemAiBroadcastService(
			UserRepository userRepository,
			MaintenanceTaskExecutionRepository maintenanceTaskExecutionRepository,
			TelegramNotificationService telegramNotificationService,
			@Value("${telegram.mini-app-url:}") String miniAppUrl) {
		this.userRepository = userRepository;
		this.maintenanceTaskExecutionRepository = maintenanceTaskExecutionRepository;
		this.telegramNotificationService = telegramNotificationService;
		this.miniAppUrl = miniAppUrl;
	}

	public BroadcastResult sendOnce() {
		validateConfiguration();
		MaintenanceTaskExecution execution = reserveExecution();
		if (execution == null) {
			log.info("Hack Alem AI broadcast skipped because taskKey={} already exists.", TASK_KEY);
			return BroadcastResult.alreadyExecuted();
		}

		List<Long> recipients = userRepository.findDistinctValidTelegramIds();
		int successfulSends = 0;
		int failedSends = 0;
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
				new InlineKeyboardButton("👥 Найти команду").webApp(new WebAppInfo(miniAppUrl.trim())));

		log.info("Hack Alem AI broadcast started: totalRecipients={}", recipients.size());
		for (Long recipientTelegramId : recipients) {
			TelegramNotificationService.SendResult result = telegramNotificationService.sendMessageWithResult(
					recipientTelegramId,
					MESSAGE,
					"hack alem ai broadcast",
					null,
					recipientTelegramId,
					null,
					null,
					keyboard,
					ParseMode.HTML);
			if (result == TelegramNotificationService.SendResult.SENT) {
				successfulSends++;
			} else {
				failedSends++;
			}
		}

		completeExecution(execution, recipients.size(), successfulSends, failedSends);
		log.info(
				"Hack Alem AI broadcast completed: totalRecipients={}, successfulSends={}, failedSends={}",
				recipients.size(),
				successfulSends,
				failedSends);
		return BroadcastResult.completed(recipients.size(), successfulSends, failedSends);
	}

	private void validateConfiguration() {
		if (!telegramNotificationService.isConfigured()) {
			throw new IllegalStateException("Telegram bot is not configured. Set TELEGRAM_BOT_TOKEN before broadcasting.");
		}
		if (miniAppUrl == null || miniAppUrl.isBlank()) {
			throw new IllegalStateException(
					"Telegram Mini App URL is not configured. Set TELEGRAM_MINI_APP_URL to the existing production Mini App URL.");
		}
	}

	@Transactional
	protected MaintenanceTaskExecution reserveExecution() {
		if (maintenanceTaskExecutionRepository.existsById(TASK_KEY)) {
			return null;
		}
		try {
			return maintenanceTaskExecutionRepository.saveAndFlush(new MaintenanceTaskExecution(TASK_KEY));
		} catch (DataIntegrityViolationException ex) {
			return null;
		}
	}

	@Transactional
	protected void completeExecution(
			MaintenanceTaskExecution execution,
			int totalRecipients,
			int successfulSends,
			int failedSends) {
		execution.setCompletedAt(LocalDateTime.now());
		execution.setTotalRecipients(totalRecipients);
		execution.setSuccessfulSends(successfulSends);
		execution.setFailedSends(failedSends);
		maintenanceTaskExecutionRepository.save(execution);
	}

	public record BroadcastResult(
			boolean skipped,
			int totalRecipients,
			int successfulSends,
			int failedSends) {

		static BroadcastResult alreadyExecuted() {
			return new BroadcastResult(true, 0, 0, 0);
		}

		static BroadcastResult completed(int totalRecipients, int successfulSends, int failedSends) {
			return new BroadcastResult(false, totalRecipients, successfulSends, failedSends);
		}
	}
}
