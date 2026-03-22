package com.findteam.findteam.service;

import com.findteam.findteam.dto.CreatePostRequest;
import com.findteam.findteam.dto.PostResponse;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

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
	 * Feed query: optional filters; all {@code null} means every post. Newest first.
	 * Runs in one transaction so lazy {@code author} can be loaded while mapping.
	 */
	@Transactional(readOnly = true)
	public List<PostResponse> getFilteredPosts(PostType type, PostGoal goal, PostStatus status) {
		Specification<Post> spec = Specification.where(PostSpecification.hasType(type))
				.and(PostSpecification.hasGoal(goal))
				.and(PostSpecification.hasStatus(status));
		return postRepository
				.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
				.stream()
				.map(this::toPostResponse)
				.toList();
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
