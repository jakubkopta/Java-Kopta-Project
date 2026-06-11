package com.example.bookstore.dto;

import java.time.LocalDateTime;

public record ReservationResponse(
		Long id,
		Long bookId,
		String bookTitle,
		String userEmail,
		String status,
		LocalDateTime createdAt,
		LocalDateTime returnedAt
) {
}
