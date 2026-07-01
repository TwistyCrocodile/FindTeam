package com.findteam.findteam.controller;

import com.findteam.findteam.dto.ApplicationResponse;
import com.findteam.findteam.dto.CreateCurrentUserPostRequest;
import com.findteam.findteam.dto.CreatePostRequest;
import com.findteam.findteam.dto.PostPageResponse;
import com.findteam.findteam.dto.PostResponse;
import com.findteam.findteam.dto.TelegramAuthUser;
import com.findteam.findteam.model.PostGoal;
import com.findteam.findteam.model.PostLanguage;
import com.findteam.findteam.model.PostStatus;
import com.findteam.findteam.model.PostType;
import com.findteam.findteam.model.UserStatus;
import com.findteam.findteam.service.ApplicationService;
import com.findteam.findteam.service.CurrentTelegramUserService;
import com.findteam.findteam.service.PostService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/posts")
public class PostController {

	private final PostService postService;
	private final ApplicationService applicationService;
	private final CurrentTelegramUserService currentTelegramUserService;

	public PostController(
			PostService postService,
			ApplicationService applicationService,
			CurrentTelegramUserService currentTelegramUserService) {
		this.postService = postService;
		this.applicationService = applicationService;
		this.currentTelegramUserService = currentTelegramUserService;
	}

	@PostMapping
	public ResponseEntity<PostResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
		PostResponse body = postService.createPost(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(body);
	}

	@PostMapping("/me")
	public ResponseEntity<PostResponse> createPostForCurrentUser(
			@RequestHeader(name = "X-Telegram-Init-Data", required = false) String initData,
			@Valid @RequestBody CreateCurrentUserPostRequest request) {
		TelegramAuthUser authUser = currentTelegramUserService.resolve(initData);
		PostResponse body = postService.createPost(authUser.telegramId(), request);
		return ResponseEntity.status(HttpStatus.CREATED).body(body);
	}

	@GetMapping
	public ResponseEntity<PostPageResponse> getPosts(
			@RequestParam(required = false) PostType type,
			@RequestParam(required = false) PostGoal goal,
			@RequestParam(required = false) PostLanguage language,
			@RequestParam(required = false) PostStatus status,
			@RequestParam(required = false) UserStatus authorStatus,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(postService.getFilteredPosts(type, goal, language, status, authorStatus, page, size));
	}

	@GetMapping("/me")
	public ResponseEntity<List<PostResponse>> getCurrentUserPosts(
			@RequestHeader(name = "X-Telegram-Init-Data", required = false) String initData) {
		TelegramAuthUser authUser = currentTelegramUserService.resolve(initData);
		return ResponseEntity.ok(postService.getPostsByAuthor(authUser.telegramId()));
	}

	@PatchMapping("/{postId}/close")
	public ResponseEntity<PostResponse> closePost(
			@PathVariable Long postId,
			@RequestParam Long telegramId) {
		return ResponseEntity.ok(postService.closePost(postId, telegramId));
	}

	@PatchMapping("/{postId}/close-secure")
	public ResponseEntity<PostResponse> closePostSecure(
			@PathVariable Long postId,
			@RequestHeader(name = "X-Telegram-Init-Data", required = false) String initData) {
		TelegramAuthUser authUser = currentTelegramUserService.resolve(initData);
		return ResponseEntity.ok(postService.closePost(postId, authUser.telegramId()));
	}

	@PatchMapping("/{postId}/reopen")
	public ResponseEntity<PostResponse> reopenPost(
			@PathVariable Long postId,
			@RequestParam Long telegramId) {
		return ResponseEntity.ok(postService.reopenPost(postId, telegramId));
	}

	@PatchMapping("/{postId}/reopen-secure")
	public ResponseEntity<PostResponse> reopenPostSecure(
			@PathVariable Long postId,
			@RequestHeader(name = "X-Telegram-Init-Data", required = false) String initData) {
		TelegramAuthUser authUser = currentTelegramUserService.resolve(initData);
		return ResponseEntity.ok(postService.reopenPost(postId, authUser.telegramId()));
	}

	@PutMapping("/{postId}")
	public ResponseEntity<PostResponse> updatePost(
			@PathVariable Long postId,
			@RequestHeader(name = "X-Telegram-Init-Data", required = false) String initData,
			@RequestParam(required = false) Long telegramId,
			@Valid @RequestBody CreateCurrentUserPostRequest request) {
		if ((initData == null || initData.isBlank()) && telegramId == null) {
			return ResponseEntity.badRequest().build();
		}
		Long requesterTelegramId = resolveRequesterTelegramId(initData, telegramId);
		return ResponseEntity.ok(postService.updatePost(postId, requesterTelegramId, request));
	}

	@DeleteMapping("/{postId}")
	public ResponseEntity<Void> deletePost(
			@PathVariable Long postId,
			@RequestParam Long telegramId) {
		postService.deletePost(postId, telegramId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{postId}/secure")
	public ResponseEntity<Void> deletePostSecure(
			@PathVariable Long postId,
			@RequestHeader(name = "X-Telegram-Init-Data", required = false) String initData) {
		TelegramAuthUser authUser = currentTelegramUserService.resolve(initData);
		postService.deletePost(postId, authUser.telegramId());
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{postId}/applications")
	public ResponseEntity<List<ApplicationResponse>> getApplicationsForPost(
			@PathVariable Long postId,
			@RequestParam Long telegramId) {
		return ResponseEntity.ok(applicationService.getApplicationsForPost(postId, telegramId));
	}

	@GetMapping("/{postId}/applications/secure")
	public ResponseEntity<List<ApplicationResponse>> getApplicationsForPostSecure(
			@PathVariable Long postId,
			@RequestHeader(name = "X-Telegram-Init-Data", required = false) String initData) {
		TelegramAuthUser authUser = currentTelegramUserService.resolve(initData);
		return ResponseEntity.ok(applicationService.getApplicationsForPost(postId, authUser.telegramId()));
	}

	private Long resolveRequesterTelegramId(String initData, Long telegramId) {
		if (initData != null && !initData.isBlank()) {
			return currentTelegramUserService.resolve(initData).telegramId();
		}
		return telegramId;
	}
}
