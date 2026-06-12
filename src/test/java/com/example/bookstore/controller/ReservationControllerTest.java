package com.example.bookstore.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.bookstore.config.SecurityConfig;
import com.example.bookstore.dto.ReservationResponse;
import com.example.bookstore.security.CustomUserDetailsService;
import com.example.bookstore.security.JwtService;
import com.example.bookstore.service.ReservationService;

@WebMvcTest(ReservationController.class)
@Import({SecurityConfig.class, com.example.bookstore.exception.GlobalExceptionHandler.class})
class ReservationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ReservationService reservationService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private CustomUserDetailsService customUserDetailsService;

	@Test
	@WithMockUser(roles = "USER")
	void createReservation_asUser_returnsCreated() throws Exception {
		when(reservationService.createReservation(any())).thenReturn(sampleResponse());

		mockMvc.perform(post("/api/reservations")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "bookId": 1
								}
								"""))
				.andExpect(status().isCreated());
	}

	@Test
	@WithMockUser(roles = "USER")
	void getMyReservations_returnsOk() throws Exception {
		when(reservationService.getMyReservations()).thenReturn(List.of(sampleResponse()));

		mockMvc.perform(get("/api/reservations/my"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void getAllReservations_asAdmin_returnsOk() throws Exception {
		when(reservationService.getAllReservations()).thenReturn(List.of(sampleResponse()));

		mockMvc.perform(get("/api/reservations"))
				.andExpect(status().isOk());
	}

	private ReservationResponse sampleResponse() {
		return new ReservationResponse(1L, 1L, "Book", "user@test.com", "ACTIVE", LocalDateTime.now(), null);
	}
}
