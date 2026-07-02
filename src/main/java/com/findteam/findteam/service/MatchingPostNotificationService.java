package com.findteam.findteam.service;

import com.findteam.findteam.model.User;
import com.findteam.findteam.notification.MatchingPostNotification;
import com.findteam.findteam.repository.UserRepository;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MatchingPostNotificationService {

	private final UserRepository userRepository;
	private final NotificationService notificationService;
	private final TechnologyInterestMatcher technologyInterestMatcher;

	public MatchingPostNotificationService(
			UserRepository userRepository,
			NotificationService notificationService,
			TechnologyInterestMatcher technologyInterestMatcher) {
		this.userRepository = userRepository;
		this.notificationService = notificationService;
		this.technologyInterestMatcher = technologyInterestMatcher;
	}

	@Transactional(readOnly = true)
	public void notifyInterestedUsers(Long postId, String postTitle, String postStack, Long authorTelegramId) {
		Set<String> stackTokens = technologyInterestMatcher.tokenize(postStack);
		notifyInterestedUsersForStackTokens(postId, postTitle, stackTokens, authorTelegramId);
	}

	@Transactional(readOnly = true)
	public void notifyInterestedUsersForStackTokens(
			Long postId, String postTitle, Set<String> stackTokens, Long authorTelegramId) {
		if (stackTokens.isEmpty()) {
			return;
		}

		List<User> users = userRepository.findUsersWithInterestedStacksExceptAuthor(authorTelegramId);
		for (User user : users) {
			List<String> matchedTechnologies =
					technologyInterestMatcher.matchingTechnologies(user.getInterestedStacks(), stackTokens);
			if (matchedTechnologies.isEmpty()) {
				continue;
			}
			notificationService.notifyMatchingPost(new MatchingPostNotification(
					user.getTelegramId(),
					user.getPreferredLanguage(),
					authorTelegramId,
					postId,
					postTitle,
					matchedTechnologies));
		}
	}
}
