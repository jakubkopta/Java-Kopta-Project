package com.example.bookstore.dto;

import java.time.LocalDateTime;

public record BookResponse(
		Long id,
		String title,
		String author,
		String isbn,
		Integer totalCopies,
		Integer availableCopies,
		LocalDateTime createdAt
) {
}
