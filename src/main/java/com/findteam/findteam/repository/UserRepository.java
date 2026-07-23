package com.findteam.findteam.repository;

import com.findteam.findteam.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByTelegramId(Long telegramId);

	Optional<User> findByNickname(String nickname);

	@Modifying
	@Transactional
	@Query("""
			update User u
			set u.contactTelegramUsername = :username
			where u.telegramId = :telegramId
			and (u.contactTelegramUsername is null or trim(u.contactTelegramUsername) = '')
			""")
	int setContactTelegramUsernameIfMissing(
			@Param("telegramId") Long telegramId,
			@Param("username") String username);

	@Query("""
			select distinct u.telegramId
			from User u
			where u.telegramId is not null
			and u.telegramId > 0
			""")
	List<Long> findDistinctValidTelegramIds();

	@EntityGraph(attributePaths = "interestedStacks")
	@Query("""
			select distinct u
			from User u
			join u.interestedStacks technology
			where u.telegramId <> :authorTelegramId
			""")
	List<User> findUsersWithInterestedStacksExceptAuthor(@Param("authorTelegramId") Long authorTelegramId);
}
