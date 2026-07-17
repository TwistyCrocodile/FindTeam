package com.findteam.findteam.service;

import com.findteam.findteam.model.MaintenanceTaskExecution;
import com.findteam.findteam.repository.MaintenanceTaskExecutionRepository;
import com.findteam.findteam.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrometheusJulyAiChallengeLinkBroadcastService {

	public static final String TASK_KEY = "prometheus-july-ai-link-2026-07";

	private static final Logger log = LoggerFactory.getLogger(PrometheusJulyAiChallengeLinkBroadcastService.class);

	private static final String MESSAGE = """
			🔗 Мы забыли добавить ссылку на хакатон в предыдущем сообщении 😅

			Страница Prometheus July AI Challenge:

			https://prometheus-july-ai-challenge.devpost.com/

			Удачи всем участникам! 🚀
			""";

	private final UserRepository userRepository;
	private final MaintenanceTaskExecutionRepository maintenanceTaskExecutionRepository;
	private final TelegramNotificationService telegramNotificationService;

	public PrometheusJulyAiChallengeLinkBroadcastService(
			UserRepository userRepository,
			MaintenanceTaskExecutionRepository maintenanceTaskExecutionRepository,
			TelegramNotificationService telegramNotificationService) {
		this.userRepository = userRepository;
		this.maintenanceTaskExecutionRepository = maintenanceTaskExecutionRepository;
		this.telegramNotificationService = telegramNotificationService;
	}

	public BroadcastResult sendOnce() {
		validateConfiguration();
		MaintenanceTaskExecution execution = reserveExecution();
		if (execution == null) {
			log.info("Prometheus July AI Challenge link broadcast skipped because taskKey={} already exists.", TASK_KEY);
			return BroadcastResult.alreadyExecuted();
		}

		List<Long> recipients = userRepository.findDistinctValidTelegramIds();
		int successfulSends = 0;
		int failedSends = 0;

		log.info("Prometheus July AI Challenge link broadcast started: totalRecipients={}", recipients.size());
		for (Long recipientTelegramId : recipients) {
			TelegramNotificationService.SendResult result = telegramNotificationService.sendMessageWithResult(
					recipientTelegramId,
					MESSAGE,
					"prometheus july ai challenge link broadcast",
					null,
					recipientTelegramId,
					null,
					null,
					null);
			if (result == TelegramNotificationService.SendResult.SENT) {
				successfulSends++;
			} else {
				failedSends++;
			}
		}

		completeExecution(execution, recipients.size(), successfulSends, failedSends);
		log.info(
				"Prometheus July AI Challenge link broadcast completed: totalRecipients={}, successfulSends={}, failedSends={}",
				recipients.size(),
				successfulSends,
				failedSends);
		return BroadcastResult.completed(recipients.size(), successfulSends, failedSends);
	}

	private void validateConfiguration() {
		if (!telegramNotificationService.isConfigured()) {
			throw new IllegalStateException("Telegram bot is not configured. Set TELEGRAM_BOT_TOKEN before broadcasting.");
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
