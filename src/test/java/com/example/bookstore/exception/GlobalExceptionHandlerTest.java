package com.example.bookstore.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

class GlobalExceptionHandlerTest {

	private GlobalExceptionHandler handler;

	@BeforeEach
	void setUp() {
		handler = new GlobalExceptionHandler();
	}

	@Test
	void handleNotFound_returns404() {
		ResponseEntity<?> response = handler.handleNotFound(new ResourceNotFoundException("not found"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void handleDuplicate_returns409() {
		ResponseEntity<?> response = handler.handleDuplicate(new DuplicateResourceException("duplicate"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
	}

	@Test
	void handleIllegalArgument_returns400() {
		ResponseEntity<?> response = handler.handleIllegalArgument(new IllegalArgumentException("bad request"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void handleUnauthorized_returns401() {
		ResponseEntity<?> response = handler.handleUnauthorized(new BadCredentialsException("bad"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void handleAccessDenied_returns403() {
		ResponseEntity<?> response = handler.handleAccessDenied(new AccessDeniedException("denied"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}
}
