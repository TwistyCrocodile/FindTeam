package com.findteam.findteam.repository;

import com.findteam.findteam.model.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

	List<Post> findByAuthor_TelegramIdOrderByCreatedAtDesc(Long telegramId);
}
