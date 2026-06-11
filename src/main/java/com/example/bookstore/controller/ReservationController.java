package com.example.bookstore.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.dto.ReservationRequest;
import com.example.bookstore.dto.ReservationResponse;
import com.example.bookstore.service.ReservationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Book reservation management")
public class ReservationController {

	private final ReservationService reservationService;

	public ReservationController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}

	@PostMapping
	@PreAuthorize("hasRole('USER')")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Reserve a book")
	public ReservationResponse createReservation(@Valid @RequestBody ReservationRequest request) {
		return reservationService.createReservation(request);
	}

	@GetMapping("/my")
	@PreAuthorize("hasRole('USER')")
	@Operation(summary = "Get current user's reservations")
	public List<ReservationResponse> getMyReservations() {
		return reservationService.getMyReservations();
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Get all reservations (admin only)")
	public List<ReservationResponse> getAllReservations() {
		return reservationService.getAllReservations();
	}

	@PostMapping("/{id}/return")
	@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
	@Operation(summary = "Return a reserved book")
	public ReservationResponse returnBook(@PathVariable Long id) {
		return reservationService.returnBook(id);
	}
}
