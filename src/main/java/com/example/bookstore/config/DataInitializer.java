package com.example.bookstore.config;

import java.time.LocalDateTime;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.bookstore.model.Role;
import com.example.bookstore.model.User;
import com.example.bookstore.repository.UserRepository;

@Component
public class DataInitializer implements ApplicationRunner {

	private static final String ADMIN_EMAIL = "admin@bookstore.com";
	private static final String ADMIN_PASSWORD = "admin123";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
			userRepository.save(User.builder()
					.email(ADMIN_EMAIL)
					.password(passwordEncoder.encode(ADMIN_PASSWORD))
					.role(Role.ADMIN)
					.createdAt(LocalDateTime.now())
					.build());
		}
	}
}
