package com.example.bookstore.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.bookstore.model.Role;
import com.example.bookstore.model.User;

class JwtServiceTest {

	private JwtService jwtService;
	private UserPrincipal userPrincipal;

	@BeforeEach
	void setUp() {
		jwtService = new JwtService("w+QL/vZfLL429WbS+kU/r/fwg0Y0NSfCvBPJzXF30yQ=", 3_600_000);
		User user = User.builder().email("user@test.com").role(Role.USER).build();
		userPrincipal = new UserPrincipal(user);
	}

	@Test
	void generateToken_andValidateToken() {
		String token = jwtService.generateToken(userPrincipal);

		assertThat(token).isNotBlank();
		assertThat(jwtService.extractUsername(token)).isEqualTo("user@test.com");
		assertThat(jwtService.isTokenValid(token, userPrincipal)).isTrue();
	}

	@Test
	void isTokenValid_withWrongUser_returnsFalse() {
		String token = jwtService.generateToken(userPrincipal);
		User otherUser = User.builder().email("other@test.com").role(Role.USER).build();

		assertThat(jwtService.isTokenValid(token, new UserPrincipal(otherUser))).isFalse();
	}
}
