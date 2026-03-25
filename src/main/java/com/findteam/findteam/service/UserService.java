package com.findteam.findteam.service;

import com.findteam.findteam.dto.CreateUserProfileRequest;
import com.findteam.findteam.dto.UpdateUserProfileRequest;
import com.findteam.findteam.dto.UserProfileResponse;
import com.findteam.findteam.exception.UserAlreadyExistsException;
import com.findteam.findteam.exception.UserNotFoundException;
import com.findteam.findteam.exception.NicknameAlreadyTakenException;
import com.findteam.findteam.model.User;
import com.findteam.findteam.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Transactional
	public UserProfileResponse createUserProfile(CreateUserProfileRequest request) {
		Long telegramId = request.getTelegramId();
		if (userRepository.findByTelegramId(telegramId).isPresent()) {
			throw new UserAlreadyExistsException(telegramId);
		}

		if (userRepository.findByNickname(request.getNickname()).isPresent()) {
			throw new NicknameAlreadyTakenException(request.getNickname());
		}

		User user = new User();
		user.setTelegramId(telegramId);
		user.setNickname(request.getNickname());
		user.setBio(request.getBio());
		user.setStack(request.getStack());
		user.setGithubUrl(request.getGithubUrl());

		User saved = userRepository.save(user);
		return toUserProfileResponse(saved);
	}

	@Transactional
	public UserProfileResponse updateUserProfile(Long telegramId, UpdateUserProfileRequest request) {
		User user = userRepository.findByTelegramId(telegramId)
				.orElseThrow(() -> new UserNotFoundException(telegramId));

		userRepository.findByNickname(request.getNickname()).ifPresent(existing -> {
			if (!existing.getId().equals(user.getId())) {
				throw new NicknameAlreadyTakenException(request.getNickname());
			}
		});

		user.setNickname(request.getNickname());
		user.setBio(request.getBio());
		user.setStack(request.getStack());
		user.setGithubUrl(request.getGithubUrl());

		User saved = userRepository.save(user);
		return toUserProfileResponse(saved);
	}

	public UserProfileResponse getByTelegramId(Long telegramId) {
		return userRepository.findByTelegramId(telegramId)
				.map(this::toUserProfileResponse)
				.orElseThrow(() -> new UserNotFoundException(telegramId));
	}

	private UserProfileResponse toUserProfileResponse(User user) {
		UserProfileResponse response = new UserProfileResponse();
		response.setTelegramId(user.getTelegramId());
		response.setNickname(user.getNickname());
		response.setBio(user.getBio());
		response.setStack(user.getStack());
		response.setGithubUrl(user.getGithubUrl());
		response.setCreatedAt(user.getCreatedAt());
		return response;
	}
}
