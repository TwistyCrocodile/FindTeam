package com.findteam.findteam.service;

import com.findteam.findteam.dto.CreateCurrentUserPostRequest;
import com.findteam.findteam.dto.CreatePostRequest;
import com.findteam.findteam.dto.PostPageResponse;
import com.findteam.findteam.dto.PostResponse;
import com.findteam.findteam.exception.InvalidPaginationException;
import com.findteam.findteam.exception.PostAccessDeniedException;
import com.findteam.findteam.exception.PostNotFoundException;
import com.findteam.findteam.exception.UserNotFoundException;
import com.findteam.findteam.model.Post;
import com.findteam.findteam.model.PostGoal;
import com.findteam.findteam.model.PostLanguage;
import com.findteam.findteam.model.PostStatus;
import com.findteam.findteam.model.PostType;
import com.findteam.findteam.model.User;
import com.findteam.findteam.model.UserStatus;
import com.findteam.findteam.repository.ApplicationRepository;
import com.findteam.findteam.repository.PostRepository;
import com.findteam.findteam.repository.UserRepository;
import com.findteam.findteam.specification.PostSpecification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

	private static final int MAX_PAGE_SIZE = 50;

	private final UserRepository userRepository;
	private final PostRepository postRepository;
	private final ApplicationRepository applicationRepository;

	public PostService(
			UserRepository userRepository,
			PostRepository postRepository,
			ApplicationRepository applicationRepository) {
		this.userRepository = userRepository;
		this.postRepository = postRepository;
		this.applicationRepository = applicationRepository;
	}

	@Transactional
	public PostResponse createPost(CreatePostRequest request) {
		return createPostForUser(
				request.getTelegramId(),
				request.getType(),
				request.getTitle(),
				request.getDescription(),
				request.getStack(),
				request.getGoal(),
				request.getEventLink());
	}

	@Transactional
	public PostResponse createPost(Long authorTelegramId, CreateCurrentUserPostRequest request) {
		return createPostForUser(
				authorTelegramId,
				request.getType(),
				request.getTitle(),
				request.getDescription(),
				request.getStack(),
				request.getGoal(),
				request.getEventLink());
	}

	private PostResponse createPostForUser(
			Long authorTelegramId,
			PostType type,
			String title,
			String description,
			String stack,
			PostGoal goal,
			String eventLink) {
		User author = userRepository
				.findByTelegramId(authorTelegramId)
				.orElseThrow(() -> new UserNotFoundException(authorTelegramId));
		Post post = new Post();
		post.setAuthor(author);
		post.setType(type);
		post.setTitle(title);
		post.setDescription(description);
		post.setLanguage(detectLanguage(title, description));
		post.setStack(stack);
		post.setGoal(goal);
		post.setStatus(PostStatus.ACTIVE);
		post.setEventLink(eventLink);

		Post saved = postRepository.save(post);
		return toPostResponse(saved);
	}

	/**
	 * Paginated feed: optional filters; all filter {@code null} means no filter. Newest {@code createdAt} first.
	 * {@code page} is zero-based. {@code size} is capped at 50 to limit load.
	 */
	@Transactional(readOnly = true)
	public PostPageResponse getFilteredPosts(
			PostType type,
			PostGoal goal,
			PostLanguage language,
			PostStatus status,
			UserStatus authorStatus,
			int page,
			int size) {
		if (page < 0) {
			throw new InvalidPaginationException("page must be greater than or equal to 0");
		}
		if (size <= 0) {
			throw new InvalidPaginationException("size must be greater than 0");
		}
		int effectiveSize = Math.min(size, MAX_PAGE_SIZE);

		Specification<Post> spec = Specification.where(PostSpecification.hasType(type))
				.and(PostSpecification.hasGoal(goal))
				.and(PostSpecification.hasLanguage(language))
				.and(PostSpecification.hasStatus(status))
				.and(PostSpecification.hasAuthorStatus(authorStatus));

		Pageable pageable = PageRequest.of(page, effectiveSize, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<Post> result = postRepository.findAll(spec, pageable);

		List<PostResponse> content = result.getContent().stream().map(this::toPostResponse).toList();
		return new PostPageResponse(
				content,
				result.getNumber(),
				result.getSize(),
				result.getTotalElements(),
				result.getTotalPages(),
				result.isLast());
	}

	@Transactional(readOnly = true)
	public List<PostResponse> getPostsByAuthor(Long authorTelegramId) {
		return postRepository.findByAuthor_TelegramIdOrderByCreatedAtDesc(authorTelegramId).stream()
				.map(this::toPostResponse)
				.toList();
	}

	/**
	 * Public profile feed: only {@link PostStatus#ACTIVE} posts, newest first.
	 * Closed posts are hidden from public view (MVP privacy / relevance).
	 */
	@Transactional(readOnly = true)
	public List<PostResponse> getPublicPostsByTelegramId(Long authorTelegramId) {
		userRepository
				.findByTelegramId(authorTelegramId)
				.orElseThrow(() -> new UserNotFoundException(authorTelegramId));
		return postRepository
				.findByAuthor_TelegramIdAndStatusOrderByCreatedAtDesc(authorTelegramId, PostStatus.ACTIVE)
				.stream()
				.map(this::toPostResponse)
				.toList();
	}

	@Transactional
	public PostResponse closePost(Long postId, Long requesterTelegramId) {
		Post post = getPostOrThrow(postId);
		assertRequesterIsOwner(post, requesterTelegramId);
		post.setStatus(PostStatus.CLOSED);
		return toPostResponse(postRepository.save(post));
	}

	@Transactional
	public PostResponse reopenPost(Long postId, Long requesterTelegramId) {
		Post post = getPostOrThrow(postId);
		assertRequesterIsOwner(post, requesterTelegramId);
		post.setStatus(PostStatus.ACTIVE);
		return toPostResponse(postRepository.save(post));
	}

	@Transactional
	public PostResponse updatePost(Long postId, Long requesterTelegramId, CreateCurrentUserPostRequest request) {
		Post post = getPostOrThrow(postId);
		assertRequesterIsOwner(post, requesterTelegramId);
		post.setType(request.getType());
		post.setTitle(request.getTitle());
		post.setDescription(request.getDescription());
		post.setLanguage(detectLanguage(request.getTitle(), request.getDescription()));
		post.setStack(request.getStack());
		post.setGoal(request.getGoal());
		post.setEventLink(request.getEventLink());
		return toPostResponse(postRepository.save(post));
	}

	@Transactional
	public PostResponse deletePost(Long postId, Long requesterTelegramId) {
		Post post = getPostOrThrow(postId);
		assertRequesterIsOwner(post, requesterTelegramId);
		PostResponse response = toPostResponse(post);
		applicationRepository.deleteByPostId(postId);
		postRepository.delete(post);
		return response;
	}

	private void assertRequesterIsOwner(Post post, Long requesterTelegramId) {
		// Secure endpoints pass a Telegram ID resolved from verified initData.
		// Legacy endpoints still pass the explicit MVP telegramId for local/dev compatibility.
		Long authorTelegramId = post.getAuthor().getTelegramId();
		if (!authorTelegramId.equals(requesterTelegramId)) {
			throw new PostAccessDeniedException(post.getId(), requesterTelegramId);
		}
	}

	private Post getPostOrThrow(Long postId) {
		return postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
	}

	private PostResponse toPostResponse(Post post) {
		User author = post.getAuthor();
		PostResponse response = new PostResponse();
		response.setId(post.getId());
		response.setTelegramId(author.getTelegramId());
		response.setNickname(author.getNickname());
		response.setAuthorStatus(resolveAuthorStatus(author));
		response.setType(post.getType());
		response.setTitle(post.getTitle());
		response.setDescription(post.getDescription());
		response.setLanguage(post.getLanguage());
		response.setStack(post.getStack());
		response.setGoal(post.getGoal());
		response.setStatus(post.getStatus());
		response.setEventLink(post.getEventLink());
		response.setCreatedAt(post.getCreatedAt());
		return response;
	}

	private UserStatus resolveAuthorStatus(User author) {
		return author.getStatus() == null ? UserStatus.OPEN_TO_OFFERS : author.getStatus();
	}

	private PostLanguage detectLanguage(String title, String description) {
		String text = ((title == null ? "" : title) + " " + (description == null ? "" : description));
		long cyrillicLetters = text.codePoints()
				.filter(Character::isLetter)
				.filter(codePoint -> Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.CYRILLIC)
				.count();
		long latinLetters = text.codePoints()
				.filter(Character::isLetter)
				.filter(codePoint -> Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.LATIN)
				.count();
		return cyrillicLetters > latinLetters ? PostLanguage.RU : PostLanguage.EN;
	}
}
