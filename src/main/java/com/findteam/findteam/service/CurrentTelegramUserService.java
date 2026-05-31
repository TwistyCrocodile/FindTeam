package com.findteam.findteam.service;

import com.findteam.findteam.dto.TelegramAuthUser;
import org.springframework.stereotype.Service;

@Service
public class CurrentTelegramUserService {

	private final TelegramInitDataService telegramInitDataService;

	public CurrentTelegramUserService(TelegramInitDataService telegramInitDataService) {
		this.telegramInitDataService = telegramInitDataService;
	}

	public TelegramAuthUser resolve(String rawInitData) {
		return telegramInitDataService.verifyAndExtractUser(rawInitData);
	}
}

