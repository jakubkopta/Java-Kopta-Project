package com.example.bookstore.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.bookstore.model.Role;
import com.example.bookstore.model.User;
import com.example.bookstore.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private CustomUserDetailsService userDetailsService;

	@Test
	void loadUserByUsername_whenFound_returnsPrincipal() {
		User user = User.builder().email("user@test.com").role(Role.USER).build();
		when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

		var result = userDetailsService.loadUserByUsername("user@test.com");

		assertThat(result.getUsername()).isEqualTo("user@test.com");
		assertThat(result.getAuthorities()).extracting("authority").contains("ROLE_USER");
	}

	@Test
	void loadUserByUsername_whenNotFound_throwsException() {
		when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@test.com"))
				.isInstanceOf(UsernameNotFoundException.class);
	}
}
