package com.findteam.findteam.repository;

import com.findteam.findteam.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByTelegramId(Long telegramId);
}
