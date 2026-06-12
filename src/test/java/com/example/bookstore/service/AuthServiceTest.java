package com.example.bookstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.bookstore.dto.AuthResponse;
import com.example.bookstore.dto.LoginRequest;
import com.example.bookstore.dto.RegisterRequest;
import com.example.bookstore.exception.DuplicateResourceException;
import com.example.bookstore.model.Role;
import com.example.bookstore.model.User;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.security.JwtService;
import com.example.bookstore.security.UserPrincipal;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private JwtService jwtService;

	@InjectMocks
	private AuthService authService;

	@Test
	void register_createsUserAndReturnsToken() {
		RegisterRequest request = new RegisterRequest("user@test.com", "password123");
		when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
		when(passwordEncoder.encode("password123")).thenReturn("encoded");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("jwt-token");

		AuthResponse response = authService.register(request);

		assertThat(response.token()).isEqualTo("jwt-token");
		assertThat(response.email()).isEqualTo("user@test.com");
		assertThat(response.role()).isEqualTo("USER");
	}

	@Test
	void register_whenEmailExists_throwsDuplicateException() {
		RegisterRequest request = new RegisterRequest("user@test.com", "password123");
		when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

		assertThatThrownBy(() -> authService.register(request))
				.isInstanceOf(DuplicateResourceException.class);
	}

	@Test
	void login_returnsToken() {
		User user = User.builder().email("user@test.com").role(Role.USER).build();
		UserPrincipal principal = new UserPrincipal(user);
		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
		when(authenticationManager.authenticate(any())).thenReturn(authentication);
		when(jwtService.generateToken(principal)).thenReturn("jwt-token");

		AuthResponse response = authService.login(new LoginRequest("user@test.com", "password123"));

		assertThat(response.token()).isEqualTo("jwt-token");
		assertThat(response.email()).isEqualTo("user@test.com");
	}
}
