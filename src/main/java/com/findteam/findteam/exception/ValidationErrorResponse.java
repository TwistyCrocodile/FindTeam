package com.findteam.findteam.exception;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API payload for request body validation failures (HTTP 400).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorResponse {

	private String message;
	private LocalDateTime timestamp;
	/** Field name (request property) → first validation message for that field. */
	private Map<String, String> errors;
}
