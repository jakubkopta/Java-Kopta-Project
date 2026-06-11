package com.example.bookstore.dto;

import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
		@NotNull Long bookId
) {
}
