package com.findteam.findteam.service;

import com.findteam.findteam.dto.CreatePostRequest;
import com.findteam.findteam.dto.PostPageResponse;
import com.findteam.findteam.dto.PostResponse;
import com.findteam.findteam.exception.InvalidPaginationException;
import com.findteam.findteam.exception.PostNotFoundException;
import com.findteam.findteam.exception.UserNotFoundException;
import com.findteam.findteam.model.Post;
import com.findteam.findteam.model.PostGoal;
import com.findteam.findteam.model.PostStatus;
import com.findteam.findteam.model.PostType;
import com.findteam.findteam.model.User;
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

	public PostService(UserRepository userRepository, PostRepository postRepository) {
		this.userRepository = userRepository;
		this.postRepository = postRepository;
	}

	@Transactional
	public PostResponse createPost(CreatePostRequest request) {
		User author = userRepository
				.findByTelegramId(request.getTelegramId())
				.orElseThrow(() -> new UserNotFoundException(request.getTelegramId()));

		Post post = new Post();
		post.setAuthor(author);
		post.setType(request.getType());
		post.setTitle(request.getTitle());
		post.setDescription(request.getDescription());
		post.setStack(request.getStack());
		post.setGoal(request.getGoal());
		post.setStatus(PostStatus.ACTIVE);
		post.setEventLink(request.getEventLink());

		Post saved = postRepository.save(post);
		return toPostResponse(saved);
	}

	/**
	 * Paginated feed: optional filters; all filter {@code null} means no filter. Newest {@code createdAt} first.
	 * {@code page} is zero-based. {@code size} is capped at 50 to limit load.
	 */
	@Transactional(readOnly = true)
	public PostPageResponse getFilteredPosts(PostType type, PostGoal goal, PostStatus status, int page, int size) {
		if (page < 0) {
			throw new InvalidPaginationException("page must be greater than or equal to 0");
		}
		if (size <= 0) {
			throw new InvalidPaginationException("size must be greater than 0");
		}
		int effectiveSize = Math.min(size, MAX_PAGE_SIZE);

		Specification<Post> spec = Specification.where(PostSpecification.hasType(type))
				.and(PostSpecification.hasGoal(goal))
				.and(PostSpecification.hasStatus(status));

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

	@Transactional
	public PostResponse closePost(Long postId) {
		Post post = getPostOrThrow(postId);
		post.setStatus(PostStatus.CLOSED);
		return toPostResponse(postRepository.save(post));
	}

	@Transactional
	public PostResponse reopenPost(Long postId) {
		Post post = getPostOrThrow(postId);
		post.setStatus(PostStatus.ACTIVE);
		return toPostResponse(postRepository.save(post));
	}

	private Post getPostOrThrow(Long postId) {
		return postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException(postId));
	}

	private PostResponse toPostResponse(Post post) {
		User author = post.getAuthor();
		PostResponse response = new PostResponse();
		response.setId(post.getId());
		response.setTelegramId(author.getTelegramId());
		response.setUsername(author.getUsername());
		response.setType(post.getType());
		response.setTitle(post.getTitle());
		response.setDescription(post.getDescription());
		response.setStack(post.getStack());
		response.setGoal(post.getGoal());
		response.setStatus(post.getStatus());
		response.setEventLink(post.getEventLink());
		response.setCreatedAt(post.getCreatedAt());
		return response;
	}
}
