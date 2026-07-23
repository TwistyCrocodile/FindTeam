package com.findteam.findteam.service;

import com.findteam.findteam.dto.TelegramAuthUser;
import com.findteam.findteam.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentTelegramUserService {

	private final TelegramInitDataService telegramInitDataService;
	private final UserRepository userRepository;

	public CurrentTelegramUserService(
			TelegramInitDataService telegramInitDataService,
			UserRepository userRepository) {
		this.telegramInitDataService = telegramInitDataService;
		this.userRepository = userRepository;
	}

	@Transactional
	public TelegramAuthUser resolve(String rawInitData) {
		TelegramAuthUser authUser = telegramInitDataService.verifyAndExtractUser(rawInitData);
		String username = normalizeTelegramUsername(authUser.username());
		if (username != null) {
			userRepository.setContactTelegramUsernameIfMissing(authUser.telegramId(), username);
		}
		return authUser;
	}

	private String normalizeTelegramUsername(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim().replaceFirst("^@", "");
	}
}
