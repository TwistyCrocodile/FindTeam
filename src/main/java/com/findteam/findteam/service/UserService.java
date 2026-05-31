package com.findteam.findteam.service;

import com.findteam.findteam.dto.ContactInfoResponse;
import com.findteam.findteam.dto.CreateUserProfileRequest;
import com.findteam.findteam.dto.RegisterCurrentUserRequest;
import com.findteam.findteam.dto.RegisterUserRequest;
import com.findteam.findteam.dto.UpdateContactInfoRequest;
import com.findteam.findteam.dto.UpdateUserProfileRequest;
import com.findteam.findteam.dto.UserProfileResponse;
import com.findteam.findteam.exception.NicknameAlreadyTakenException;
import com.findteam.findteam.exception.UserAlreadyExistsException;
import com.findteam.findteam.exception.UserNotFoundException;
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
		RegisterUserRequest registerRequest = new RegisterUserRequest();
		registerRequest.setTelegramId(request.getTelegramId());
		registerRequest.setNickname(request.getNickname());
		registerRequest.setBio(request.getBio());
		registerRequest.setStack(request.getStack());
		registerRequest.setGithubUrl(request.getGithubUrl());
		return registerUser(registerRequest);
	}

	@Transactional
	public UserProfileResponse registerUser(RegisterUserRequest request) {
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
	public UserProfileResponse registerCurrentUser(Long telegramId, RegisterCurrentUserRequest request) {
		RegisterUserRequest registerRequest = new RegisterUserRequest();
		registerRequest.setTelegramId(telegramId);
		registerRequest.setNickname(request.getNickname());
		registerRequest.setBio(request.getBio());
		registerRequest.setStack(request.getStack());
		registerRequest.setGithubUrl(request.getGithubUrl());
		return registerUser(registerRequest);
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

	public ContactInfoResponse getContactInfo(Long telegramId) {
		return userRepository.findByTelegramId(telegramId)
				.map(this::toContactInfoResponse)
				.orElseThrow(() -> new UserNotFoundException(telegramId));
	}

	@Transactional
	public ContactInfoResponse updateContactInfo(Long telegramId, UpdateContactInfoRequest request) {
		User user = userRepository.findByTelegramId(telegramId)
				.orElseThrow(() -> new UserNotFoundException(telegramId));

		user.setContactTelegramUsername(normalizeTelegramUsername(request.getContactTelegramUsername()));
		user.setContactGithubUrl(normalizeBlank(request.getContactGithubUrl()));
		user.setContactEmail(normalizeBlank(request.getContactEmail()));

		return toContactInfoResponse(userRepository.save(user));
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

	private ContactInfoResponse toContactInfoResponse(User user) {
		ContactInfoResponse response = new ContactInfoResponse();
		response.setContactTelegramUsername(user.getContactTelegramUsername());
		response.setContactGithubUrl(user.getContactGithubUrl());
		response.setContactEmail(user.getContactEmail());
		return response;
	}

	private String normalizeTelegramUsername(String value) {
		String normalized = normalizeBlank(value);
		return normalized == null ? null : normalized.replaceFirst("^@", "");
	}

	private String normalizeBlank(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
