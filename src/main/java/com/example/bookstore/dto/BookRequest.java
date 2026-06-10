package com.example.bookstore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookRequest(
		@NotBlank String title,
		@NotBlank String author,
		@NotBlank @Size(max = 20) String isbn,
		@NotNull @Min(0) Integer totalCopies
) {
}
