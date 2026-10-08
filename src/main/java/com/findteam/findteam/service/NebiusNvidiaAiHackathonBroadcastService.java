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
public class NebiusNvidiaAiHackathonBroadcastService {

	public static final String TASK_KEY = "nebius-nvidia-ai-hackathon-2026-10";

	private static final Logger log = LoggerFactory.getLogger(NebiusNvidiaAiHackathonBroadcastService.class);

	private static final String HACKATHON_URL = "https://nebiusglobalaihackathon.devpost.com/";
	private static final String MESSAGE = """
			🚀 <b>NVIDIA × Nebius — глобальный AI-хакатон с призами $50,000+!</b>

			Хочешь создать собственного AI-агента, умного ассистента или приложение на базе искусственного интеллекта? Тогда это для тебя! 🔥

			🌍 <b>Формат:</b> онлайн, международный  
			💰 <b>Призовой фонд:</b> $50,000+  
			📅 <b>Дедлайн:</b> 30 октября 2026  
			🏆 <b>Главный приз:</b> $20,000

			<b>Направления:</b>

			💻 Coding &amp; AI Agents — AI-инструменты для разработчиков  
			🤖 Apps &amp; Agents — приложения и автономные агенты  
			🧠 Personal AI — персональные AI-ассистенты  
			⚙️ Physical AI — робототехника и IoT

			Для участия нужно создать работающий проект с использованием моделей NVIDIA на инфраструктуре Nebius.

			🤝 <b>Нет команды?</b>

			Найди разработчиков, дизайнеров и единомышленников через FindTeam!

			👉 @FindTeamForYou_bot

			🔗 <b>Регистрация и подробности:</b>  
			<a href="https://nebiusglobalaihackathon.devpost.com/">https://nebiusglobalaihackathon.devpost.com/</a>

			⚠️ Участие только для совершеннолетних.
			""";

	private final UserRepository userRepository;
	private final MaintenanceTaskExecutionRepository maintenanceTaskExecutionRepository;
	private final TelegramNotificationService telegramNotificationService;
	private final String miniAppUrl;

	public NebiusNvidiaAiHackathonBroadcastService(
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
			log.info("Nebius x NVIDIA AI Hackathon broadcast skipped because taskKey={} already exists.", TASK_KEY);
			return BroadcastResult.alreadyExecuted();
		}

		List<Long> recipients = userRepository.findDistinctValidTelegramIds();
		int successfulSends = 0;
		int failedSends = 0;
		InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup(
				new InlineKeyboardButton("👥 Найти команду").webApp(new WebAppInfo(miniAppUrl.trim())),
				new InlineKeyboardButton("🚀 О хакатоне").url(HACKATHON_URL));

		log.info("Nebius x NVIDIA AI Hackathon broadcast started: totalRecipients={}", recipients.size());
		for (Long recipientTelegramId : recipients) {
			TelegramNotificationService.SendResult result = telegramNotificationService.sendMessageWithResult(
					recipientTelegramId,
					MESSAGE,
					"nebius x nvidia ai hackathon broadcast",
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
				"Nebius x NVIDIA AI Hackathon broadcast completed: totalRecipients={}, successfulSends={}, failedSends={}",
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
