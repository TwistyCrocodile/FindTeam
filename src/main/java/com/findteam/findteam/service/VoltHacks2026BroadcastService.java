package com.findteam.findteam.service;

import com.findteam.findteam.model.MaintenanceTaskExecution;
import com.findteam.findteam.repository.MaintenanceTaskExecutionRepository;
import com.findteam.findteam.repository.UserRepository;
import com.pengrad.telegrambot.model.WebAppInfo;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoltHacks2026BroadcastService {

	public static final String TASK_KEY = "volthacks-2026-2026-07";

	private static final Logger log = LoggerFactory.getLogger(VoltHacks2026BroadcastService.class);

	private static final String HACKATHON_URL = "https://volthacks.devpost.com/";
	private static final String MESSAGE = """
			⚡ **VoltHacks 2026 — международный онлайн-хакатон по AI, IoT и робототехнике**

			Если давно хотели попробовать себя в международном хакатоне — отличный шанс.

			🌍 Формат: полностью онлайн
			👤 Участвовать могут школьники и студенты от 13 лет
			📅 Дедлайн: **6 сентября 2026**
			👥 Можно участвовать как командой, так и индивидуально

			Основные направления:
			• Artificial Intelligence & Machine Learning
			• IoT (Internet of Things)
			• Robotics & Automation
			• Embedded Systems
			• Smart Health
			• Sustainability
			• Smart Cities
			• Open Innovation

			Принимаются как программные, так и hardware-проекты. Наличие собственного оборудования необязательно — можно создать полностью программное решение.

			Организаторы заявляют призовой фонд более **$32,000** (денежные призы, спонсорские кредиты, подписки и другие награды). Среди судей — специалисты из Apple, Meta, Microsoft, Amazon, LinkedIn, AWS, Oracle, TikTok и других крупных компаний.

			Если у вас ещё нет команды, создайте пост в FindTeam и найдите backend-, frontend-, mobile-, ML-, embedded-разработчиков или дизайнера для совместной разработки.

			🔗 https://volthacks.devpost.com/\
			""";

	private final UserRepository userRepository;
	private final MaintenanceTaskExecutionRepository maintenanceTaskExecutionRepository;
	private final TelegramNotificationService telegramNotificationService;
	private final String miniAppUrl;

	public VoltHacks2026BroadcastService(
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
			log.info("VoltHacks 2026 broadcast skipped because taskKey={} already exists.", TASK_KEY);
			return BroadcastResult.alreadyExecuted();
		}

		List<Long> recipients = userRepository.findDistinctValidTelegramIds();
		int successfulSends = 0;
		int failedSends = 0;
		InlineKeyboardMarkup keyboard = keyboard();

		log.info("VoltHacks 2026 broadcast started: totalRecipients={}", recipients.size());
		for (Long recipientTelegramId : recipients) {
			TelegramNotificationService.SendResult result = telegramNotificationService.sendMessageWithResult(
					recipientTelegramId,
					MESSAGE,
					"volthacks 2026 broadcast",
					null,
					recipientTelegramId,
					null,
					null,
					keyboard);
			if (result == TelegramNotificationService.SendResult.SENT) {
				successfulSends++;
			} else {
				failedSends++;
			}
		}

		completeExecution(execution, recipients.size(), successfulSends, failedSends);
		log.info(
				"VoltHacks 2026 broadcast completed: totalRecipients={}, successfulSends={}, failedSends={}",
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

	private InlineKeyboardMarkup keyboard() {
		return new InlineKeyboardMarkup(
				new InlineKeyboardButton("👥 Найти команду").webApp(new WebAppInfo(miniAppUrl.trim())),
				new InlineKeyboardButton("⚡ О хакатоне").url(HACKATHON_URL)
		);
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
