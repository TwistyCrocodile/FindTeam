package com.findteam.findteam.config;

import com.findteam.findteam.model.PostGoal;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("postgres")
public class PostGoalCheckConstraintMigration implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(PostGoalCheckConstraintMigration.class);

	private static final String CONSTRAINT_DEFINITION_SQL = """
			SELECT pg_get_constraintdef(c.oid)
			FROM pg_constraint c
			JOIN pg_class t ON t.oid = c.conrelid
			JOIN pg_namespace n ON n.oid = t.relnamespace
			WHERE n.nspname = current_schema()
			  AND t.relname = 'posts'
			  AND c.conname = 'posts_goal_check'
			  AND c.contype = 'c'
			""";

	private static final String DROP_POSTS_GOAL_CHECK_SQL =
			"ALTER TABLE posts DROP CONSTRAINT IF EXISTS posts_goal_check";

	private static final String ADD_POSTS_GOAL_CHECK_SQL = """
			ALTER TABLE posts ADD CONSTRAINT posts_goal_check
			CHECK (goal IN ('HACKATHON', 'STARTUP', 'PET_PROJECT', 'JOB', 'OLYMPIAD'))
			""";

	private final JdbcTemplate jdbcTemplate;

	public PostGoalCheckConstraintMigration(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run(ApplicationArguments args) {
		List<String> definitions = jdbcTemplate.queryForList(CONSTRAINT_DEFINITION_SQL, String.class);
		if (!definitions.isEmpty() && allowsAllPostGoals(definitions.get(0))) {
			return;
		}

		log.info("Updating posts_goal_check constraint to include current PostGoal values");
		jdbcTemplate.execute(DROP_POSTS_GOAL_CHECK_SQL);
		jdbcTemplate.execute(ADD_POSTS_GOAL_CHECK_SQL);
	}

	private boolean allowsAllPostGoals(String constraintDefinition) {
		return Arrays.stream(PostGoal.values()).map(PostGoal::name).allMatch(constraintDefinition::contains);
	}
}
