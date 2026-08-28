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
public class ZubrCapitalAiHackathonBroadcastService {

	public static final String TASK_KEY = "zubr-capital-ai-hackathon-2026-09";

	private static final Logger log = LoggerFactory.getLogger(ZubrCapitalAiHackathonBroadcastService.class);

	private static final String HACKATHON_URL = "https://league.zubrcapital.com/hackathon";
	private static final String MESSAGE = """
			🤖 <b>AI Hackathon Zubr Capital Young — однодневный AI-хакатон в Алматы</b>

			19 сентября в Алматы пройдет бесплатный хакатон для студентов и молодых фаундеров. За один день участники будут создавать AI-продукты, проверять идеи, собирать MVP и презентовать проекты перед экспертами и инвесторами.

			📍 Алматы, офлайн
			📅 19 сентября 2026
			🕤 9:30–20:00
			🗣 Язык: русский
			💰 Участие бесплатное
			📝 Заявки до 12 сентября

			Можно прийти без команды и даже без опыта программирования — организаторы помогут сформировать команды из участников с разными навыками.

			Подойдут разработчики, дизайнеры, маркетологи, аналитики и все, кто хочет попробовать создать свой первый AI-стартап.

			🔗 Подробнее и регистрация:
			https://league.zubrcapital.com/hackathon

			Если ищете людей для участия — создавайте пост в FindTeam и собирайте команду заранее.
			""";

	private final UserRepository userRepository;
	private final MaintenanceTaskExecutionRepository maintenanceTaskExecutionRepository;
	private final TelegramNotificationService telegramNotificationService;
	private final String miniAppUrl;

	public ZubrCapitalAiHackathonBroadcastService(
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
			log.info("Zubr Capital AI Hackathon broadcast skipped because taskKey={} already exists.", TASK_KEY);
			return BroadcastResult.alreadyExecuted();
		}

		List<Long> recipients = userRepository.findDistinctValidTelegramIds();
		int successfulSends = 0;
		int failedSends = 0;
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
				new InlineKeyboardButton("👥 Найти команду").webApp(new WebAppInfo(miniAppUrl.trim())));

		log.info("Zubr Capital AI Hackathon broadcast started: totalRecipients={}", recipients.size());
		for (Long recipientTelegramId : recipients) {
			TelegramNotificationService.SendResult result = telegramNotificationService.sendMessageWithResult(
					recipientTelegramId,
					MESSAGE,
					"zubr capital ai hackathon broadcast",
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
				"Zubr Capital AI Hackathon broadcast completed: totalRecipients={}, successfulSends={}, failedSends={}",
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
