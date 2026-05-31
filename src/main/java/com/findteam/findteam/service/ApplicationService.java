package com.findteam.findteam.service;

import com.findteam.findteam.dto.ApplicationResponse;
import com.findteam.findteam.exception.ApplicationAlreadyExistsException;
import com.findteam.findteam.exception.ApplicationNotFoundException;
import com.findteam.findteam.exception.CannotApplyToClosedPostException;
import com.findteam.findteam.exception.CannotApplyToOwnPostException;
import com.findteam.findteam.exception.PostAccessDeniedException;
import com.findteam.findteam.exception.PostNotFoundException;
import com.findteam.findteam.exception.UserNotFoundException;
import com.findteam.findteam.model.Application;
import com.findteam.findteam.model.ApplicationStatus;
import com.findteam.findteam.model.Post;
import com.findteam.findteam.model.PostStatus;
import com.findteam.findteam.model.User;
import com.findteam.findteam.repository.ApplicationRepository;
import com.findteam.findteam.repository.PostRepository;
import com.findteam.findteam.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApplicationService {

	private final ApplicationRepository applicationRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;

	public ApplicationService(
			ApplicationRepository applicationRepository,
			PostRepository postRepository,
			UserRepository userRepository) {
		this.applicationRepository = applicationRepository;
		this.postRepository = postRepository;
		this.userRepository = userRepository;
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
		return toApplicationResponse(saved);
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
		response.setStatus(application.getStatus());
		response.setContactAvailable(application.getStatus() == ApplicationStatus.ACCEPTED);
		response.setCreatedAt(application.getCreatedAt());
		return response;
	}
}
