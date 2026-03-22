package com.findteam.findteam.controller;

import com.findteam.findteam.dto.CreatePostRequest;
import com.findteam.findteam.dto.PostPageResponse;
import com.findteam.findteam.dto.PostResponse;
import com.findteam.findteam.model.PostGoal;
import com.findteam.findteam.model.PostStatus;
import com.findteam.findteam.model.PostType;
import com.findteam.findteam.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {

	private final PostService postService;

	public PostController(PostService postService) {
		this.postService = postService;
	}

	@PostMapping
	public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
		PostResponse body = postService.createPost(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(body);
	}

	@GetMapping
	public ResponseEntity<PostPageResponse> getPosts(
			@RequestParam(required = false) PostType type,
			@RequestParam(required = false) PostGoal goal,
			@RequestParam(required = false) PostStatus status,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(postService.getFilteredPosts(type, goal, status, page, size));
	}
}
