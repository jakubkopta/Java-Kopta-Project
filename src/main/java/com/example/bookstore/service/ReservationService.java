package com.example.bookstore.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookstore.dto.ReservationRequest;
import com.example.bookstore.dto.ReservationResponse;
import com.example.bookstore.exception.ResourceNotFoundException;
import com.example.bookstore.model.Book;
import com.example.bookstore.model.Reservation;
import com.example.bookstore.model.ReservationStatus;
import com.example.bookstore.model.Role;
import com.example.bookstore.model.User;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.ReservationRepository;
import com.example.bookstore.security.SecurityUtils;

@Service
@Transactional
public class ReservationService {

	private static final Set<ReservationStatus> ACTIVE_STATUSES = Set.of(
			ReservationStatus.PENDING,
			ReservationStatus.ACTIVE);

	private final ReservationRepository reservationRepository;
	private final BookRepository bookRepository;

	public ReservationService(ReservationRepository reservationRepository, BookRepository bookRepository) {
		this.reservationRepository = reservationRepository;
		this.bookRepository = bookRepository;
	}

	public ReservationResponse createReservation(ReservationRequest request) {
		User user = SecurityUtils.getCurrentUser();
		Book book = findBookOrThrow(request.bookId());

		if (book.getAvailableCopies() <= 0) {
			throw new IllegalArgumentException("No available copies for book: " + book.getTitle());
		}

		if (reservationRepository.existsByUserIdAndBookIdAndStatusIn(user.getId(), book.getId(), ACTIVE_STATUSES)) {
			throw new IllegalArgumentException("You already have an active reservation for this book");
		}

		Reservation reservation = Reservation.builder()
				.user(user)
				.book(book)
				.status(ReservationStatus.ACTIVE)
				.createdAt(LocalDateTime.now())
				.build();

		book.setAvailableCopies(book.getAvailableCopies() - 1);
		bookRepository.save(book);

		return toResponse(reservationRepository.save(reservation));
	}

	@Transactional(readOnly = true)
	public List<ReservationResponse> getMyReservations() {
		User user = SecurityUtils.getCurrentUser();
		return reservationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<ReservationResponse> getAllReservations() {
		return reservationRepository.findAllByOrderByCreatedAtDesc().stream()
				.map(this::toResponse)
				.toList();
	}

	public ReservationResponse returnBook(Long reservationId) {
		User currentUser = SecurityUtils.getCurrentUser();
		Reservation reservation = findReservationOrThrow(reservationId);

		if (!reservation.getUser().getId().equals(currentUser.getId())
				&& currentUser.getRole() != Role.ADMIN) {
			throw new AccessDeniedException("You can only return your own reservations");
		}

		if (reservation.getStatus() != ReservationStatus.ACTIVE
				&& reservation.getStatus() != ReservationStatus.PENDING) {
			throw new IllegalArgumentException("Only active reservations can be returned");
		}

		Book book = reservation.getBook();
		book.setAvailableCopies(book.getAvailableCopies() + 1);
		bookRepository.save(book);

		reservation.setStatus(ReservationStatus.RETURNED);
		reservation.setReturnedAt(LocalDateTime.now());

		return toResponse(reservationRepository.save(reservation));
	}

	private Book findBookOrThrow(Long bookId) {
		return bookRepository.findById(bookId)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));
	}

	private Reservation findReservationOrThrow(Long reservationId) {
		return reservationRepository.findById(reservationId)
				.orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));
	}

	private ReservationResponse toResponse(Reservation reservation) {
		return new ReservationResponse(
				reservation.getId(),
				reservation.getBook().getId(),
				reservation.getBook().getTitle(),
				reservation.getUser().getEmail(),
				reservation.getStatus().name(),
				reservation.getCreatedAt(),
				reservation.getReturnedAt());
	}
}
