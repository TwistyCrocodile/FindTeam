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

	public String buildApplicationAcceptedMessage(
			PreferredLanguage recipientLanguage,
			String postTitle,
			String authorName) {
		Messages messages = messagesFor(recipientLanguage);

		StringBuilder message = new StringBuilder();
		message.append(messages.applicationAcceptedTitle());
		message.append("\n\n");
		message.append(messages.applicationAcceptedBody());
		message.append("\n\n");
		message.append("🏆 ");
		message.append(safeText(postTitle));
		message.append("\n\n");
		message.append(messages.projectAuthorLabel());
		message.append("\n");
		message.append(safeText(authorName));
		message.append("\n\n");
		message.append(messages.contactProjectAuthor());
		message.append("\n\n");
		message.append(messages.goodLuck());
		return message.toString();
	}

	public String buildApplicationRejectedMessage(
			PreferredLanguage recipientLanguage,
			String postTitle) {
		Messages messages = messagesFor(recipientLanguage);

		StringBuilder message = new StringBuilder();
		message.append(messages.applicationRejectedTitle());
		message.append("\n\n");
		message.append(messages.applicationRejectedBody());
		message.append("\n\n");
		message.append("🏆 ");
		message.append(safeText(postTitle));
		message.append("\n\n");
		message.append(messages.applyToOtherPosts());
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
				"Open FindTeam to review the application.",
				"🎉 Good news!",
				"Your application was accepted.",
				"👤 Project author",
				"You can now contact the project author in FindTeam.",
				"Good luck! 🚀",
				"❌ Application rejected",
				"Your application was rejected.",
				"You can still apply to other posts in FindTeam."),
		RU(
				"🎉 Новый отклик!",
				"На ваш пост:",
				"поступил новый отклик.",
				"👤 Кандидат",
				"💻 Стек",
				"Откройте FindTeam, чтобы просмотреть заявку.",
				"🎉 Хорошие новости!",
				"Ваш отклик приняли.",
				"👤 Автор проекта",
				"Теперь вы можете связаться с автором проекта в FindTeam.",
				"Удачи! 🚀",
				"❌ Отклик отклонён",
				"Ваш отклик отклонили.",
				"Вы всё ещё можете откликаться на другие посты в FindTeam.");

		private final String newApplicationTitle;
		private final String yourPost;
		private final String receivedApplication;
		private final String applicantLabel;
		private final String stackLabel;
		private final String reviewApplication;
		private final String applicationAcceptedTitle;
		private final String applicationAcceptedBody;
		private final String projectAuthorLabel;
		private final String contactProjectAuthor;
		private final String goodLuck;
		private final String applicationRejectedTitle;
		private final String applicationRejectedBody;
		private final String applyToOtherPosts;

		Messages(
				String newApplicationTitle,
				String yourPost,
				String receivedApplication,
				String applicantLabel,
				String stackLabel,
				String reviewApplication,
				String applicationAcceptedTitle,
				String applicationAcceptedBody,
				String projectAuthorLabel,
				String contactProjectAuthor,
				String goodLuck,
				String applicationRejectedTitle,
				String applicationRejectedBody,
				String applyToOtherPosts) {
			this.newApplicationTitle = newApplicationTitle;
			this.yourPost = yourPost;
			this.receivedApplication = receivedApplication;
			this.applicantLabel = applicantLabel;
			this.stackLabel = stackLabel;
			this.reviewApplication = reviewApplication;
			this.applicationAcceptedTitle = applicationAcceptedTitle;
			this.applicationAcceptedBody = applicationAcceptedBody;
			this.projectAuthorLabel = projectAuthorLabel;
			this.contactProjectAuthor = contactProjectAuthor;
			this.goodLuck = goodLuck;
			this.applicationRejectedTitle = applicationRejectedTitle;
			this.applicationRejectedBody = applicationRejectedBody;
			this.applyToOtherPosts = applyToOtherPosts;
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

		private String applicationAcceptedTitle() {
			return applicationAcceptedTitle;
		}

		private String applicationAcceptedBody() {
			return applicationAcceptedBody;
		}

		private String projectAuthorLabel() {
			return projectAuthorLabel;
		}

		private String contactProjectAuthor() {
			return contactProjectAuthor;
		}

		private String goodLuck() {
			return goodLuck;
		}

		private String applicationRejectedTitle() {
			return applicationRejectedTitle;
		}

		private String applicationRejectedBody() {
			return applicationRejectedBody;
		}

		private String applyToOtherPosts() {
			return applyToOtherPosts;
		}
	}
}
