package com.example.bookstore.dto;

public record AuthResponse(
		String token,
		String email,
		String role
) {
}
