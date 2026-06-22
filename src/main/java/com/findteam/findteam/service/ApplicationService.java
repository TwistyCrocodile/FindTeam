package com.findteam.findteam.service;

import com.findteam.findteam.dto.ApplicationResponse;
import com.findteam.findteam.dto.ContactInfoResponse;
import com.findteam.findteam.exception.ApplicationAlreadyExistsException;
import com.findteam.findteam.exception.ApplicationNotFoundException;
import com.findteam.findteam.exception.CannotApplyToClosedPostException;
import com.findteam.findteam.exception.CannotApplyToOwnPostException;
import com.findteam.findteam.exception.ContactNotAvailableException;
import com.findteam.findteam.exception.PostAccessDeniedException;
import com.findteam.findteam.exception.PostNotFoundException;
import com.findteam.findteam.exception.UserNotFoundException;
import com.findteam.findteam.model.Application;
import com.findteam.findteam.model.ApplicationStatus;
import com.findteam.findteam.model.Post;
import com.findteam.findteam.model.PostStatus;
import com.findteam.findteam.model.User;
import com.findteam.findteam.model.UserStatus;
import com.findteam.findteam.repository.ApplicationRepository;
import com.findteam.findteam.repository.PostRepository;
import com.findteam.findteam.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class ApplicationService {

	private final ApplicationRepository applicationRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final TelegramNotificationService telegramNotificationService;

	public ApplicationService(
			ApplicationRepository applicationRepository,
			PostRepository postRepository,
			UserRepository userRepository,
			TelegramNotificationService telegramNotificationService) {
		this.applicationRepository = applicationRepository;
		this.postRepository = postRepository;
		this.userRepository = userRepository;
		this.telegramNotificationService = telegramNotificationService;
	}

	@Transactional
	public ApplicationResponse applyToPost(Long postId, Long applicantTelegramId) {
		Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
		User applicant = userRepository
				.findByTelegramId(applicantTelegramId)
				.orElseThrow(() -> new UserNotFoundException(applicantTelegramId));

		if (post.getStatus() == PostStatus.CLOSED) {
			throw new CannotApplyToClosedPostException(postId);
		}

		if (post.getAuthor().getTelegramId().equals(applicantTelegramId)) {
			throw new CannotApplyToOwnPostException(postId);
		}

		if (applicationRepository.findByPost_IdAndApplicant_TelegramId(postId, applicantTelegramId).isPresent()) {
			throw new ApplicationAlreadyExistsException(postId, applicantTelegramId);
		}

		Application application = new Application();
		application.setPost(post);
		application.setApplicant(applicant);
		application.setStatus(ApplicationStatus.PENDING);

		Application saved = applicationRepository.save(application);
		notifyAuthorAfterCommit(saved);
		return toApplicationResponse(saved);
	}

	private void notifyAuthorAfterCommit(Application application) {
		Post post = application.getPost();
		User author = post.getAuthor();
		User applicant = application.getApplicant();
		Long authorTelegramId = author.getTelegramId();
		Long applicantTelegramId = applicant.getTelegramId();
		Long postId = post.getId();
		String postTitle = post.getTitle();
		String applicantNickname = applicant.getNickname();
		String applicantTelegramUsername = applicant.getContactTelegramUsername();
		String applicantStack = applicant.getStack();
		Runnable notification = () -> telegramNotificationService.notifyNewApplication(
				authorTelegramId,
				applicantTelegramId,
				postId,
				postTitle,
				applicantNickname,
				applicantTelegramUsername,
				applicantStack);

		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			notification.run();
			return;
		}

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				notification.run();
			}
		});
	}

	@Transactional(readOnly = true)
	public List<ApplicationResponse> getApplicationsForPost(Long postId, Long requesterTelegramId) {
		Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
		assertRequesterIsOwner(post, requesterTelegramId);

		return applicationRepository.findByPost_IdOrderByCreatedAtDesc(postId).stream()
				.map(this::toApplicationResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<ApplicationResponse> getApplicationsByApplicant(Long applicantTelegramId) {
		return applicationRepository.findByApplicant_TelegramIdOrderByCreatedAtDesc(applicantTelegramId).stream()
				.map(this::toApplicationResponse)
				.toList();
	}

	@Transactional
	public ApplicationResponse acceptApplication(Long applicationId, Long requesterTelegramId) {
		Application application = getApplicationOrThrow(applicationId);
		assertRequesterIsOwner(application.getPost(), requesterTelegramId);
		application.setStatus(ApplicationStatus.ACCEPTED);
		return toApplicationResponse(applicationRepository.save(application));
	}

	@Transactional
	public ApplicationResponse rejectApplication(Long applicationId, Long requesterTelegramId) {
		Application application = getApplicationOrThrow(applicationId);
		assertRequesterIsOwner(application.getPost(), requesterTelegramId);
		application.setStatus(ApplicationStatus.REJECTED);
		return toApplicationResponse(applicationRepository.save(application));
	}

	@Transactional(readOnly = true)
	public ContactInfoResponse getUnlockedContactInfo(Long applicationId, Long requesterTelegramId) {
		Application application = getApplicationOrThrow(applicationId);
		if (application.getStatus() != ApplicationStatus.ACCEPTED) {
			throw ContactNotAvailableException.notAccepted(applicationId);
		}

		User owner = application.getPost().getAuthor();
		User applicant = application.getApplicant();
		if (owner.getTelegramId().equals(requesterTelegramId)) {
			return toContactInfoResponse(applicant);
		}
		if (applicant.getTelegramId().equals(requesterTelegramId)) {
			return toContactInfoResponse(owner);
		}
		throw ContactNotAvailableException.accessDenied(applicationId, requesterTelegramId);
	}

	private Application getApplicationOrThrow(Long applicationId) {
		return applicationRepository.findById(applicationId)
				.orElseThrow(() -> new ApplicationNotFoundException(applicationId));
	}

	private void assertRequesterIsOwner(Post post, Long requesterTelegramId) {
		// Secure endpoints pass a Telegram ID resolved from verified initData.
		// Legacy endpoints still pass the explicit MVP telegramId for local/dev compatibility.
		Long authorTelegramId = post.getAuthor().getTelegramId();
		if (!authorTelegramId.equals(requesterTelegramId)) {
			throw new PostAccessDeniedException(post.getId(), requesterTelegramId);
		}
	}

	private ApplicationResponse toApplicationResponse(Application application) {
		User applicant = application.getApplicant();
		Post post = application.getPost();
		ApplicationResponse response = new ApplicationResponse();
		response.setId(application.getId());
		response.setPostId(post.getId());
		response.setPostTitle(post.getTitle());
		response.setPostGoal(post.getGoal());
		response.setPostStatus(post.getStatus());
		response.setApplicantNickname(applicant.getNickname());
		response.setApplicantTelegramId(applicant.getTelegramId());
		response.setApplicantStatus(resolveUserStatus(applicant));
		response.setStatus(application.getStatus());
		response.setContactAvailable(application.getStatus() == ApplicationStatus.ACCEPTED);
		response.setCreatedAt(application.getCreatedAt());
		return response;
	}

	private UserStatus resolveUserStatus(User user) {
		return user.getStatus() == null ? UserStatus.OPEN_TO_OFFERS : user.getStatus();
	}

	private ContactInfoResponse toContactInfoResponse(User user) {
		ContactInfoResponse response = new ContactInfoResponse();
		response.setContactTelegramUsername(user.getContactTelegramUsername());
		response.setContactEmail(user.getContactEmail());
		return response;
	}
}
