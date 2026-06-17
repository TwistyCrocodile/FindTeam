package com.findteam.findteam.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "author_id", nullable = false)
	private User author;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private PostType type;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(nullable = false, length = 1000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(length = 2)
	private PostLanguage language;

	@Column(nullable = false, length = 255)
	private String stack;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private PostGoal goal;

	/** Lifecycle for listings and future flows (e.g. closing a filled role, moderation). */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private PostStatus status = PostStatus.ACTIVE;

	@Column(name = "event_link", length = 255)
	private String eventLink;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		if (status == null) {
			status = PostStatus.ACTIVE;
		}
		createdAt = LocalDateTime.now();
	}
}
