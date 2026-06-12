package com.example.bookstore.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.bookstore.config.SecurityConfig;
import com.example.bookstore.dto.AuthResponse;
import com.example.bookstore.security.CustomUserDetailsService;
import com.example.bookstore.security.JwtService;
import com.example.bookstore.service.AuthService;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, com.example.bookstore.exception.GlobalExceptionHandler.class})
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private CustomUserDetailsService customUserDetailsService;

	@Test
	void register_returnsCreated() throws Exception {
		when(authService.register(any())).thenReturn(new AuthResponse("token", "user@test.com", "USER"));

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "user@test.com",
								  "password": "password123"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.token").value("token"));
	}

	@Test
	void login_returnsOk() throws Exception {
		when(authService.login(any())).thenReturn(new AuthResponse("token", "user@test.com", "USER"));

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "user@test.com",
								  "password": "password123"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("user@test.com"));
	}
}
