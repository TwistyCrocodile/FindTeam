package com.findteam.findteam.service;

import com.findteam.findteam.dto.CreatePostRequest;
import com.findteam.findteam.dto.PostResponse;
import com.findteam.findteam.exception.UserNotFoundException;
import com.findteam.findteam.model.Post;
import com.findteam.findteam.model.PostStatus;
import com.findteam.findteam.model.User;
import com.findteam.findteam.repository.PostRepository;
import com.findteam.findteam.repository.UserRepository;
import java.util.List;
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
	 * Loads posts and maps to DTOs in one transaction so lazy {@code author} can be initialized.
	 */
	@Transactional(readOnly = true)
	public List<PostResponse> getAllPosts() {
		return postRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toPostResponse).toList();
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
