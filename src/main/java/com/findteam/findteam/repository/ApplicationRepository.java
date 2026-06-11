package com.findteam.findteam.repository;

import com.findteam.findteam.model.Application;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

	List<Application> findByPost_IdOrderByCreatedAtDesc(Long postId);

	List<Application> findByApplicant_TelegramIdOrderByCreatedAtDesc(Long telegramId);

	Optional<Application> findByPost_IdAndApplicant_TelegramId(Long postId, Long applicantTelegramId);

	long deleteByPost_Id(Long postId);
}
