package com.findteam.findteam.specification;

import com.findteam.findteam.model.Post;
import com.findteam.findteam.model.PostGoal;
import com.findteam.findteam.model.PostLanguage;
import com.findteam.findteam.model.PostStatus;
import com.findteam.findteam.model.PostType;
import org.springframework.data.jpa.domain.Specification;

/**
 * Builds optional {@link Specification} fragments for post feeds; {@code null} arguments mean "no filter".
 */
public final class PostSpecification {

	private PostSpecification() {}

	public static Specification<Post> hasType(PostType type) {
		return (root, query, cb) -> type == null ? cb.conjunction() : cb.equal(root.get("type"), type);
	}

	public static Specification<Post> hasGoal(PostGoal goal) {
		return (root, query, cb) -> goal == null ? cb.conjunction() : cb.equal(root.get("goal"), goal);
	}

	public static Specification<Post> hasStatus(PostStatus status) {
		return (root, query, cb) -> status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
	}

	public static Specification<Post> hasLanguage(PostLanguage language) {
		return (root, query, cb) -> language == null ? cb.conjunction() : cb.equal(root.get("language"), language);
	}
}
