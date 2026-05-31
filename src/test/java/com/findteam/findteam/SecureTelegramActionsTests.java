package com.findteam.findteam;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.findteam.findteam.model.Application;
import com.findteam.findteam.model.ApplicationStatus;
import com.findteam.findteam.model.Post;
import com.findteam.findteam.model.PostGoal;
import com.findteam.findteam.model.PostStatus;
import com.findteam.findteam.model.PostType;
import com.findteam.findteam.model.User;
import com.findteam.findteam.repository.ApplicationRepository;
import com.findteam.findteam.repository.PostRepository;
import com.findteam.findteam.repository.UserRepository;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "telegram.bot-token=test-token")
@AutoConfigureMockMvc
class SecureTelegramActionsTests {

	private static final String INIT_DATA_HEADER = "X-Telegram-Init-Data";
	private static final String BOT_TOKEN = "test-token";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private ApplicationRepository applicationRepository;

	@BeforeEach
	void cleanDatabase() {
		applicationRepository.deleteAll();
		postRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void securePostCloseUsesVerifiedOwnerTelegramId() throws Exception {
		User owner = saveUser(1001L, "owner_user");
		saveUser(2002L, "other_user");
		Post post = savePost(owner, PostStatus.ACTIVE);

		mockMvc.perform(patch("/api/posts/{postId}/close-secure", post.getId())
						.header(INIT_DATA_HEADER, validInitData(owner.getTelegramId(), owner.getNickname())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("CLOSED")));

		mockMvc.perform(patch("/api/posts/{postId}/reopen-secure", post.getId())
						.header(INIT_DATA_HEADER, validInitData(2002L, "other_user")))
				.andExpect(status().isForbidden());
	}

	@Test
	void secureApplicationAcceptUsesVerifiedPostOwnerTelegramId() throws Exception {
		User owner = saveUser(3003L, "post_owner");
		User applicant = saveUser(4004L, "applicant_user");
		User other = saveUser(5005L, "not_owner");
		Post post = savePost(owner, PostStatus.ACTIVE);
		Application application = saveApplication(post, applicant);

		mockMvc.perform(patch("/api/applications/{applicationId}/reject-secure", application.getId())
						.header(INIT_DATA_HEADER, validInitData(other.getTelegramId(), other.getNickname())))
				.andExpect(status().isForbidden());

		mockMvc.perform(patch("/api/applications/{applicationId}/accept-secure", application.getId())
						.header(INIT_DATA_HEADER, validInitData(owner.getTelegramId(), owner.getNickname())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("ACCEPTED")))
				.andExpect(jsonPath("$.contactAvailable", is(true)));
	}

	@Test
	void secureEndpointsRejectInvalidInitData() throws Exception {
		mockMvc.perform(post("/api/posts/me")
						.header(INIT_DATA_HEADER, "auth_date=1&user={}&hash=bad")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "type": "SEEKING_TEAM",
								  "title": "Need teammate",
								  "description": "Building an MVP",
								  "stack": "Java, React",
								  "goal": "PET_PROJECT",
								  "eventLink": ""
								}
								"""))
				.andExpect(status().isUnauthorized());
	}

	private User saveUser(Long telegramId, String nickname) {
		User user = new User();
		user.setTelegramId(telegramId);
		user.setNickname(nickname);
		user.setBio("");
		user.setStack("Java");
		user.setGithubUrl("");
		return userRepository.save(user);
	}

	private Post savePost(User author, PostStatus status) {
		Post post = new Post();
		post.setAuthor(author);
		post.setType(PostType.SEEKING_TEAM);
		post.setTitle("Need teammate");
		post.setDescription("Building an MVP");
		post.setStack("Java, React");
		post.setGoal(PostGoal.PET_PROJECT);
		post.setStatus(status);
		post.setEventLink("");
		return postRepository.save(post);
	}

	private Application saveApplication(Post post, User applicant) {
		Application application = new Application();
		application.setPost(post);
		application.setApplicant(applicant);
		application.setStatus(ApplicationStatus.PENDING);
		return applicationRepository.save(application);
	}

	private String validInitData(Long telegramId, String username) {
		long authDate = Instant.now().getEpochSecond();
		String userJson = "{\"id\":" + telegramId + ",\"username\":\"" + username + "\",\"first_name\":\"Test\"}";
		String dataCheckString = "auth_date=" + authDate + "\nuser=" + userJson;
		String hash = hmacHex(telegramSecretKey(), dataCheckString);
		return "auth_date=" + authDate
				+ "&user=" + urlEncode(userJson)
				+ "&hash=" + hash;
	}

	private byte[] telegramSecretKey() {
		return hmacBytes("WebAppData".getBytes(StandardCharsets.UTF_8), BOT_TOKEN);
	}

	private String hmacHex(byte[] key, String value) {
		byte[] bytes = hmacBytes(key, value);
		StringBuilder hex = new StringBuilder();
		for (byte b : bytes) {
			hex.append(String.format("%02x", b));
		}
		return hex.toString();
	}

	private byte[] hmacBytes(byte[] key, String value) {
		return hmacBytes(key, value.getBytes(StandardCharsets.UTF_8));
	}

	private byte[] hmacBytes(byte[] key, byte[] value) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(key, "HmacSHA256"));
			return mac.doFinal(value);
		} catch (NoSuchAlgorithmException | InvalidKeyException ex) {
			throw new IllegalStateException("Could not calculate HMAC", ex);
		}
	}

	private String urlEncode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}

