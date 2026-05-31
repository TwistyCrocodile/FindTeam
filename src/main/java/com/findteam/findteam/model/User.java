package com.findteam.findteam.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private Long telegramId;

	/**
	 * Public nickname used inside the app.
	 * <p>
	 * Note: we currently map this to the historical {@code username} column to avoid requiring a DB migration.
	 * A future migration can rename the column once data is aligned.
	 */
	@Column(name = "username", nullable = false, unique = true, length = 32)
	private String nickname;

	private String bio;

	private String stack;

	private String githubUrl;

	private String contactTelegramUsername;

	private String contactGithubUrl;

	private String contactEmail;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
	}
}
