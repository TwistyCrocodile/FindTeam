package com.findteam.findteam.repository;

import com.findteam.findteam.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByTelegramId(Long telegramId);

	Optional<User> findByNickname(String nickname);

	@EntityGraph(attributePaths = "interestedStacks")
	@Query("""
			select distinct u
			from User u
			join u.interestedStacks technology
			where u.telegramId <> :authorTelegramId
			""")
	List<User> findUsersWithInterestedStacksExceptAuthor(@Param("authorTelegramId") Long authorTelegramId);
}
