package com.findteam.findteam.service;

import com.findteam.findteam.model.PreferredLanguage;
import org.springframework.stereotype.Service;

@Service
public class TelegramNotificationMessages {

	public String buildNewApplicationMessage(
			PreferredLanguage recipientLanguage,
			String postTitle,
			String applicantName,
			String applicantStack) {
		Messages messages = messagesFor(recipientLanguage);

		StringBuilder message = new StringBuilder();
		message.append(messages.newApplicationTitle());
		message.append("\n\n");
		message.append(messages.yourPost());
		message.append("\n\n");
		message.append("🏆 ");
		message.append(safeText(postTitle));
		message.append("\n\n");
		message.append(messages.receivedApplication());
		message.append("\n\n");
		message.append(messages.applicantLabel());
		message.append("\n");
		message.append(safeText(applicantName));

		if (hasText(applicantStack)) {
			message.append("\n\n");
			message.append(messages.stackLabel());
			message.append("\n");
			message.append(applicantStack.trim());
		}

		message.append("\n\n");
		message.append(messages.reviewApplication());
		return message.toString();
	}

	private Messages messagesFor(PreferredLanguage recipientLanguage) {
		if (recipientLanguage == PreferredLanguage.RU) {
			return Messages.RU;
		}
		return Messages.EN;
	}

	private String safeText(String value) {
		return hasText(value) ? value.trim() : "";
	}

	private boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

	private enum Messages {
		EN(
				"🎉 New application!",
				"Your post:",
				"received a new application.",
				"👤 Applicant",
				"💻 Stack",
				"Open FindTeam to review the application."),
		RU(
				"🎉 Новый отклик!",
				"На ваш пост:",
				"поступил новый отклик.",
				"👤 Кандидат",
				"💻 Стек",
				"Откройте FindTeam, чтобы просмотреть заявку.");

		private final String newApplicationTitle;
		private final String yourPost;
		private final String receivedApplication;
		private final String applicantLabel;
		private final String stackLabel;
		private final String reviewApplication;

		Messages(
				String newApplicationTitle,
				String yourPost,
				String receivedApplication,
				String applicantLabel,
				String stackLabel,
				String reviewApplication) {
			this.newApplicationTitle = newApplicationTitle;
			this.yourPost = yourPost;
			this.receivedApplication = receivedApplication;
			this.applicantLabel = applicantLabel;
			this.stackLabel = stackLabel;
			this.reviewApplication = reviewApplication;
		}

		private String newApplicationTitle() {
			return newApplicationTitle;
		}

		private String yourPost() {
			return yourPost;
		}

		private String receivedApplication() {
			return receivedApplication;
		}

		private String applicantLabel() {
			return applicantLabel;
		}

		private String stackLabel() {
			return stackLabel;
		}

		private String reviewApplication() {
			return reviewApplication;
		}
	}
}
