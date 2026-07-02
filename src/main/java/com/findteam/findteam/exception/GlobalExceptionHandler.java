package com.findteam.findteam.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {

	/** Invalid query/path parameter type (e.g. bad enum literal for {@code type}, {@code goal}, {@code status}). */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
		String name = ex.getName();
		Object value = ex.getValue();
		String detail = value != null ? " (received: " + value + ")" : "";
		String message = "Invalid value for parameter '" + name + "'" + detail;
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(message));
	}

	/** Invalid JSON body values, including enum literals such as {@code goal}. */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
		if (ex.getCause() instanceof InvalidFormatException invalidFormat
				&& invalidFormat.getTargetType().isEnum()) {
			String field = invalidFormat.getPath().stream()
					.map(JsonMappingException.Reference::getFieldName)
					.filter(name -> name != null && !name.isBlank())
					.reduce((first, second) -> second)
					.orElse("field");
			String allowedValues = Arrays.stream(invalidFormat.getTargetType().getEnumConstants())
					.map(Object::toString)
					.collect(Collectors.joining(", "));
			String message = "Invalid value for field '" + field + "' (received: " + invalidFormat.getValue()
					+ "). Allowed values: " + allowedValues;
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(message));
		}
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error("Invalid request body value"));
	}

	@ExceptionHandler(InvalidPaginationException.class)
	public ResponseEntity<ErrorResponse> handleInvalidPagination(InvalidPaginationException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage()));
	}

	@ExceptionHandler(InvalidInterestedStacksException.class)
	public ResponseEntity<ErrorResponse> handleInvalidInterestedStacks(InvalidInterestedStacksException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage()));
	}

	/**
	 * Bean Validation failed on a {@code @Valid} controller parameter (e.g. request body).
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new LinkedHashMap<>();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			// One message per field; first wins (stable order from BindingResult).
			errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
		}
		ValidationErrorResponse body =
				new ValidationErrorResponse("Validation failed", LocalDateTime.now(), errors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(ex.getMessage()));
	}

	@ExceptionHandler(PostNotFoundException.class)
	public ResponseEntity<ErrorResponse> handlePostNotFound(PostNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(ex.getMessage()));
	}

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error(ex.getMessage()));
	}

	@ExceptionHandler(NicknameAlreadyTakenException.class)
	public ResponseEntity<ErrorResponse> handleNicknameAlreadyTaken(NicknameAlreadyTakenException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error(ex.getMessage()));
	}

	@ExceptionHandler(PostAccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handlePostAccessDenied(PostAccessDeniedException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage()));
	}

	@ExceptionHandler(ApplicationAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleApplicationAlreadyExists(ApplicationAlreadyExistsException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(error(ex.getMessage()));
	}

	@ExceptionHandler(CannotApplyToOwnPostException.class)
	public ResponseEntity<ErrorResponse> handleCannotApplyToOwnPost(CannotApplyToOwnPostException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage()));
	}

	@ExceptionHandler(CannotApplyToClosedPostException.class)
	public ResponseEntity<ErrorResponse> handleCannotApplyToClosedPost(CannotApplyToClosedPostException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error(ex.getMessage()));
	}

	@ExceptionHandler(ApplicationNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleApplicationNotFound(ApplicationNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error(ex.getMessage()));
	}

	@ExceptionHandler(InvalidTelegramInitDataException.class)
	public ResponseEntity<ErrorResponse> handleInvalidTelegramInitData(InvalidTelegramInitDataException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error(ex.getMessage()));
	}

	@ExceptionHandler(ContactNotAvailableException.class)
	public ResponseEntity<ErrorResponse> handleContactNotAvailable(ContactNotAvailableException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error(ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(error("An unexpected error occurred"));
	}

	private static ErrorResponse error(String message) {
		return new ErrorResponse(message, LocalDateTime.now());
	}
}
