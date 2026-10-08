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
import com.findteam.findteam.service.NebiusNvidiaAiHackathonBroadcastService;
import com.findteam.findteam.service.TelegramNotificationService;
import com.pengrad.telegrambot.model.request.ParseMode;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NebiusNvidiaAiHackathonBroadcastServiceTests {

	@Mock
	private UserRepository userRepository;

	@Mock
	private MaintenanceTaskExecutionRepository maintenanceTaskExecutionRepository;

	@Mock
	private TelegramNotificationService telegramNotificationService;

	@Test
	void sendsHtmlFormattedMessageWithClickableOfficialUrl() {
		NebiusNvidiaAiHackathonBroadcastService service = service();
		MaintenanceTaskExecution execution = new MaintenanceTaskExecution(
				NebiusNvidiaAiHackathonBroadcastService.TASK_KEY);
		when(maintenanceTaskExecutionRepository.existsById(NebiusNvidiaAiHackathonBroadcastService.TASK_KEY))
				.thenReturn(false);
		when(maintenanceTaskExecutionRepository.saveAndFlush(any(MaintenanceTaskExecution.class)))
				.thenReturn(execution);
		when(userRepository.findDistinctValidTelegramIds()).thenReturn(List.of(1001L));
		when(telegramNotificationService.isConfigured()).thenReturn(true);
		when(telegramNotificationService.sendMessageWithResult(
				any(), any(), any(), any(), any(), any(), any(), any(), any()))
				.thenReturn(TelegramNotificationService.SendResult.SENT);

		NebiusNvidiaAiHackathonBroadcastService.BroadcastResult result = service.sendOnce();

		assertFalse(result.skipped());
		assertEquals(1, result.totalRecipients());
		assertEquals(1, result.successfulSends());
		assertEquals(0, result.failedSends());

		ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<ParseMode> parseModeCaptor = ArgumentCaptor.forClass(ParseMode.class);
		verify(telegramNotificationService).sendMessageWithResult(
				any(), messageCaptor.capture(), any(), any(), any(), any(), any(), any(), parseModeCaptor.capture());
		String message = messageCaptor.getValue();
		assertEquals(ParseMode.HTML, parseModeCaptor.getValue());
		assertTrue(message.contains("<b>NVIDIA × Nebius — глобальный AI-хакатон с призами $50,000+!</b>"));
		assertTrue(message.contains("<b>Формат:</b> онлайн, международный"));
		assertTrue(message.contains("Coding &amp; AI Agents"));
		assertTrue(message.contains(
				"<a href=\"https://nebiusglobalaihackathon.devpost.com/\">https://nebiusglobalaihackathon.devpost.com/</a>"));
	}

	@Test
	void skipsSendingWhenTaskWasAlreadyReserved() {
		NebiusNvidiaAiHackathonBroadcastService service = service();
		when(telegramNotificationService.isConfigured()).thenReturn(true);
		when(maintenanceTaskExecutionRepository.existsById(NebiusNvidiaAiHackathonBroadcastService.TASK_KEY))
				.thenReturn(true);

		NebiusNvidiaAiHackathonBroadcastService.BroadcastResult result = service.sendOnce();

		assertTrue(result.skipped());
		verify(userRepository, never()).findDistinctValidTelegramIds();
		verify(telegramNotificationService, never()).sendMessageWithResult(
				any(), any(), any(), any(), any(), any(), any(), any(), any());
	}

	private NebiusNvidiaAiHackathonBroadcastService service() {
		return new NebiusNvidiaAiHackathonBroadcastService(
				userRepository,
				maintenanceTaskExecutionRepository,
				telegramNotificationService,
				"https://findteam.example.com");
	}
}
