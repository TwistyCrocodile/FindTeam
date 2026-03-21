package com.findteam.findteam.service;

import com.findteam.findteam.dto.CreateUserRequest;
import com.findteam.findteam.dto.UserResponse;
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
	public UserResponse createUser(CreateUserRequest request) {
		Long telegramId = request.getTelegramId();
		if (userRepository.findByTelegramId(telegramId).isPresent()) {
			throw new UserAlreadyExistsException(telegramId);
		}

		User user = new User();
		user.setTelegramId(telegramId);
		user.setUsername(request.getUsername());
		user.setBio(request.getBio());
		user.setStack(request.getStack());
		user.setGithubUrl(request.getGithubUrl());

		User saved = userRepository.save(user);
		return toUserResponse(saved);
	}

	public UserResponse getByTelegramId(Long telegramId) {
		return userRepository.findByTelegramId(telegramId)
				.map(this::toUserResponse)
				.orElseThrow(() -> new UserNotFoundException(telegramId));
	}

	private UserResponse toUserResponse(User user) {
		UserResponse response = new UserResponse();
		response.setId(user.getId());
		response.setTelegramId(user.getTelegramId());
		response.setUsername(user.getUsername());
		response.setBio(user.getBio());
		response.setStack(user.getStack());
		response.setGithubUrl(user.getGithubUrl());
		response.setCreatedAt(user.getCreatedAt());
		return response;
	}
}
