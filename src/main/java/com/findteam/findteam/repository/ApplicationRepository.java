package com.findteam.findteam.repository;

import com.findteam.findteam.model.Application;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

	List<Application> findByPost_IdOrderByCreatedAtDesc(Long postId);

	List<Application> findByApplicant_TelegramIdOrderByCreatedAtDesc(Long telegramId);

	Optional<Application> findByPost_IdAndApplicant_TelegramId(Long postId, Long applicantTelegramId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("delete from Application a where a.post.id = :postId")
	int deleteByPostId(@Param("postId") Long postId);
}
