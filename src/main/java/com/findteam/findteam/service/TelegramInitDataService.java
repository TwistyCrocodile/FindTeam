package com.findteam.findteam.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.findteam.findteam.dto.TelegramAuthUser;
import com.findteam.findteam.exception.InvalidTelegramInitDataException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TelegramInitDataService {

	private static final String HMAC_SHA256 = "HmacSHA256";
	private static final long MAX_AUTH_AGE_SECONDS = 24 * 60 * 60;

	private final String botToken;
	private final ObjectMapper objectMapper;

	public TelegramInitDataService(
			@Value("${telegram.bot-token:}") String botToken,
			ObjectMapper objectMapper) {
		this.botToken = botToken;
		this.objectMapper = objectMapper;
	}

	public TelegramAuthUser verifyAndExtractUser(String rawInitData) {
		if (botToken == null || botToken.isBlank()) {
			throw new InvalidTelegramInitDataException("Telegram bot token is not configured");
		}
		if (rawInitData == null || rawInitData.isBlank()) {
			throw new InvalidTelegramInitDataException("Missing Telegram init data");
		}

		Map<String, String> params = parseQueryString(rawInitData);
		String receivedHash = params.remove("hash");
		if (receivedHash == null || receivedHash.isBlank()) {
			throw new InvalidTelegramInitDataException("Missing Telegram init data hash");
		}

		validateAuthDate(params.get("auth_date"));

		String dataCheckString = buildDataCheckString(params);
		byte[] secretKey = hmacSha256("WebAppData".getBytes(StandardCharsets.UTF_8), botToken);
		byte[] calculatedHash = hmacSha256(secretKey, dataCheckString);
		byte[] receivedHashBytes = hexToBytes(receivedHash);

		if (!MessageDigest.isEqual(calculatedHash, receivedHashBytes)) {
			throw new InvalidTelegramInitDataException("Invalid Telegram init data signature");
		}

		return parseUser(params.get("user"));
	}

	private Map<String, String> parseQueryString(String rawInitData) {
		Map<String, String> params = new HashMap<>();
		String[] pairs = rawInitData.split("&");
		for (String pair : pairs) {
			if (pair.isBlank()) {
				continue;
			}
			int idx = pair.indexOf('=');
			String rawKey = idx >= 0 ? pair.substring(0, idx) : pair;
			String rawValue = idx >= 0 ? pair.substring(idx + 1) : "";
			String key = urlDecode(rawKey);
			String value = urlDecode(rawValue);
			params.put(key, value);
		}
		return params;
	}

	private String buildDataCheckString(Map<String, String> params) {
		List<String> keys = new ArrayList<>(params.keySet());
		Collections.sort(keys);

		List<String> parts = new ArrayList<>();
		for (String key : keys) {
			parts.add(key + "=" + params.get(key));
		}
		return String.join("\n", parts);
	}

	private void validateAuthDate(String authDateValue) {
		if (authDateValue == null || authDateValue.isBlank()) {
			throw new InvalidTelegramInitDataException("Missing Telegram auth_date");
		}

		try {
			long authDate = Long.parseLong(authDateValue);
			long age = Instant.now().getEpochSecond() - authDate;
			if (age < 0 || age > MAX_AUTH_AGE_SECONDS) {
				throw new InvalidTelegramInitDataException("Telegram init data is expired");
			}
		} catch (NumberFormatException ex) {
			throw new InvalidTelegramInitDataException("Invalid Telegram auth_date");
		}
	}

	private TelegramAuthUser parseUser(String userJson) {
		if (userJson == null || userJson.isBlank()) {
			throw new InvalidTelegramInitDataException("Missing Telegram user data");
		}

		try {
			JsonNode user = objectMapper.readTree(userJson);
			JsonNode idNode = user.get("id");
			if (idNode == null || !idNode.canConvertToLong()) {
				throw new InvalidTelegramInitDataException("Missing Telegram user id");
			}
			return new TelegramAuthUser(
					idNode.asLong(),
					textOrNull(user.get("username")),
					textOrNull(user.get("first_name")),
					textOrNull(user.get("last_name")));
		} catch (InvalidTelegramInitDataException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new InvalidTelegramInitDataException("Invalid Telegram user data");
		}
	}

	private String textOrNull(JsonNode node) {
		return node == null || node.isNull() ? null : node.asText();
	}

	private byte[] hmacSha256(byte[] key, String value) {
		return hmacSha256(key, value.getBytes(StandardCharsets.UTF_8));
	}

	private byte[] hmacSha256(byte[] key, byte[] value) {
		try {
			Mac mac = Mac.getInstance(HMAC_SHA256);
			mac.init(new SecretKeySpec(key, HMAC_SHA256));
			return mac.doFinal(value);
		} catch (NoSuchAlgorithmException | InvalidKeyException ex) {
			throw new IllegalStateException("Could not calculate Telegram init data signature", ex);
		}
	}

	private byte[] hexToBytes(String hex) {
		if (hex.length() % 2 != 0) {
			throw new InvalidTelegramInitDataException("Invalid Telegram init data hash");
		}

		byte[] bytes = new byte[hex.length() / 2];
		try {
			for (int i = 0; i < hex.length(); i += 2) {
				bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
			}
			return bytes;
		} catch (NumberFormatException ex) {
			throw new InvalidTelegramInitDataException("Invalid Telegram init data hash");
		}
	}

	private String urlDecode(String value) {
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}
}

