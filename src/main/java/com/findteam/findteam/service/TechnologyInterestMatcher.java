package com.findteam.findteam.service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class TechnologyInterestMatcher {

	private static final Pattern STACK_SEPARATOR = Pattern.compile("[,;/|\\n\\s]+");

	public String normalize(String value) {
		return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
	}

	public Set<String> tokenize(String value) {
		Set<String> tokens = new LinkedHashSet<>();
		if (value == null || value.isBlank()) {
			return tokens;
		}
		Arrays.stream(STACK_SEPARATOR.split(value))
				.map(this::normalize)
				.filter(token -> !token.isBlank())
				.forEach(tokens::add);
		return tokens;
	}

	public List<String> matchingTechnologies(List<String> interestedStacks, Set<String> stackTokens) {
		return interestedStacks.stream()
				.filter(technology -> matches(technology, stackTokens))
				.toList();
	}

	private boolean matches(String technology, Set<String> stackTokens) {
		Set<String> tokens = tokenize(technology);
		String normalized = normalize(technology);
		if (!normalized.isBlank()) {
			tokens.add(normalized);
		}
		return tokens.stream().anyMatch(stackTokens::contains);
	}
}
