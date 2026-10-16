package com.findteam.findteam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.findteam.findteam.model.MaintenanceTaskExecution;
import com.findteam.findteam.repository.MaintenanceTaskExecutionRepository;
import com.findteam.findteam.repository.UserRepository;
import com.findteam.findteam.service.Decentrathon6BroadcastService;
import com.findteam.findteam.service.TelegramNotificationService;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class Decentrathon6BroadcastServiceTests {

	@Mock
	private UserRepository userRepository;

	@Mock
	private MaintenanceTaskExecutionRepository maintenanceTaskExecutionRepository;

	@Mock
	private TelegramNotificationService telegramNotificationService;

	@Test
	void sendsHtmlFormattedMessageWithOfficialUrlAndButtons() {
		Decentrathon6BroadcastService service = service();
		MaintenanceTaskExecution execution = new MaintenanceTaskExecution(Decentrathon6BroadcastService.TASK_KEY);
		when(maintenanceTaskExecutionRepository.existsById(Decentrathon6BroadcastService.TASK_KEY)).thenReturn(false);
		when(maintenanceTaskExecutionRepository.saveAndFlush(any(MaintenanceTaskExecution.class))).thenReturn(execution);
		when(userRepository.findDistinctValidTelegramIds()).thenReturn(List.of(1001L));
		when(telegramNotificationService.isConfigured()).thenReturn(true);
		when(telegramNotificationService.sendMessageWithResult(
				any(), any(), any(), any(), any(), any(), any(), any(), any()))
				.thenReturn(TelegramNotificationService.SendResult.SENT);

		Decentrathon6BroadcastService.BroadcastResult result = service.sendOnce();

		assertFalse(result.skipped());
		assertEquals(1, result.totalRecipients());
		assertEquals(1, result.successfulSends());
		assertEquals(0, result.failedSends());

		ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<InlineKeyboardMarkup> keyboardCaptor = ArgumentCaptor.forClass(InlineKeyboardMarkup.class);
		ArgumentCaptor<ParseMode> parseModeCaptor = ArgumentCaptor.forClass(ParseMode.class);
		verify(telegramNotificationService).sendMessageWithResult(
				any(), messageCaptor.capture(), any(), any(), any(), any(), any(), keyboardCaptor.capture(), parseModeCaptor.capture());

		String message = messageCaptor.getValue();
		assertEquals(ParseMode.HTML, parseModeCaptor.getValue());
		assertTrue(message.contains("<b>DECENTRATHON 6.0 — масштабный хакатон по всему Казахстану! 🚀</b>"));
		assertTrue(message.contains("30–31 октября в 19 городах Казахстана"));
		assertTrue(message.contains("<b>Регистрация до:</b> 29 октября, 23:59"));
		assertTrue(message.contains("<b>Команды:</b> до 4 человек"));
		assertTrue(message.contains("<a href=\"https://decentrathon.ai/en\">https://decentrathon.ai/en</a>"));
		assertFalse(message.contains("**"));

		InlineKeyboardButton[] buttons = keyboardCaptor.getValue().inlineKeyboard()[0];
		assertEquals("👥 Найти команду", buttons[0].text());
		assertEquals("https://findteam.example.com", buttons[0].webApp().url());
		assertEquals("🔗 О хакатоне", buttons[1].text());
		assertEquals("https://decentrathon.ai/en", buttons[1].url());
	}

	@Test
	void countsFailedDeliveriesAsFailed() {
		Decentrathon6BroadcastService service = service();
		MaintenanceTaskExecution execution = new MaintenanceTaskExecution(Decentrathon6BroadcastService.TASK_KEY);
		when(maintenanceTaskExecutionRepository.existsById(Decentrathon6BroadcastService.TASK_KEY)).thenReturn(false);
		when(maintenanceTaskExecutionRepository.saveAndFlush(any(MaintenanceTaskExecution.class))).thenReturn(execution);
		when(userRepository.findDistinctValidTelegramIds()).thenReturn(List.of(1001L, 1002L));
		when(telegramNotificationService.isConfigured()).thenReturn(true);
		when(telegramNotificationService.sendMessageWithResult(
				any(), any(), any(), any(), any(), any(), any(), any(), any()))
				.thenReturn(
						TelegramNotificationService.SendResult.SENT,
						TelegramNotificationService.SendResult.FAILED);

		Decentrathon6BroadcastService.BroadcastResult result = service.sendOnce();

		assertEquals(2, result.totalRecipients());
		assertEquals(1, result.successfulSends());
		assertEquals(1, result.failedSends());
		verify(maintenanceTaskExecutionRepository).save(execution);
		assertEquals(1, execution.getSuccessfulSends());
		assertEquals(1, execution.getFailedSends());
	}

	@Test
	void skipsSendingWhenTaskWasAlreadyReserved() {
		Decentrathon6BroadcastService service = service();
		when(telegramNotificationService.isConfigured()).thenReturn(true);
		when(maintenanceTaskExecutionRepository.existsById(Decentrathon6BroadcastService.TASK_KEY)).thenReturn(true);

		Decentrathon6BroadcastService.BroadcastResult result = service.sendOnce();

		assertTrue(result.skipped());
		verify(userRepository, never()).findDistinctValidTelegramIds();
		verify(telegramNotificationService, never()).sendMessageWithResult(
				any(), any(), any(), any(), any(), any(), any(), any(), any());
	}

	private Decentrathon6BroadcastService service() {
		return new Decentrathon6BroadcastService(
				userRepository,
				maintenanceTaskExecutionRepository,
				telegramNotificationService,
				"https://findteam.example.com");
	}
}
