package com.example.bookstore.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.bookstore.model.User;

public final class SecurityUtils {

	private SecurityUtils() {
	}

	public static User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
			throw new IllegalStateException("No authenticated user found");
		}

		return principal.getUser();
	}
}
