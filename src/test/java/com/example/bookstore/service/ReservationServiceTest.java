package com.example.bookstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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
import com.example.bookstore.security.UserPrincipal;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private BookRepository bookRepository;

	@InjectMocks
	private ReservationService reservationService;

	@BeforeEach
	void setUp() {
		setCurrentUser(user(1L, Role.USER));
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void createReservation_decrementsAvailableCopies() {
		Book book = sampleBook();
		when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
		when(reservationRepository.existsByUserIdAndBookIdAndStatusIn(eq(1L), eq(1L), any(Set.class)))
				.thenReturn(false);
		when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
			Reservation reservation = invocation.getArgument(0);
			reservation.setId(10L);
			return reservation;
		});

		ReservationResponse response = reservationService.createReservation(new ReservationRequest(1L));

		assertThat(response.bookId()).isEqualTo(1L);
		assertThat(book.getAvailableCopies()).isEqualTo(4);
		verify(bookRepository).save(book);
	}

	@Test
	void createReservation_whenNoCopiesLeft_throwsException() {
		Book book = sampleBook();
		book.setAvailableCopies(0);
		when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

		assertThatThrownBy(() -> reservationService.createReservation(new ReservationRequest(1L)))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void getMyReservations_returnsUserReservations() {
		Reservation reservation = sampleReservation();
		when(reservationRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(reservation));

		List<ReservationResponse> result = reservationService.getMyReservations();

		assertThat(result).hasSize(1);
		assertThat(result.get(0).userEmail()).isEqualTo("user@test.com");
	}

	@Test
	void returnBook_marksReservationAsReturned() {
		Reservation reservation = sampleReservation();
		when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));
		when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ReservationResponse response = reservationService.returnBook(10L);

		assertThat(response.status()).isEqualTo("RETURNED");
		assertThat(reservation.getBook().getAvailableCopies()).isEqualTo(6);
	}

	@Test
	void returnBook_whenNotOwnerAndNotAdmin_throwsAccessDenied() {
		setCurrentUser(user(2L, Role.USER));
		Reservation reservation = sampleReservation();
		when(reservationRepository.findById(10L)).thenReturn(Optional.of(reservation));

		assertThatThrownBy(() -> reservationService.returnBook(10L))
				.isInstanceOf(AccessDeniedException.class);
	}

	@Test
	void returnBook_whenBookNotFound_throwsException() {
		when(reservationRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> reservationService.returnBook(99L))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	private void setCurrentUser(User user) {
		UserPrincipal principal = new UserPrincipal(user);
		UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	private User user(Long id, Role role) {
		return User.builder()
				.id(id)
				.email("user@test.com")
				.password("encoded")
				.role(role)
				.createdAt(LocalDateTime.now())
				.build();
	}

	private Book sampleBook() {
		return Book.builder()
				.id(1L)
				.title("Test Book")
				.author("Author")
				.isbn("ISBN-1")
				.totalCopies(5)
				.availableCopies(5)
				.createdAt(LocalDateTime.now())
				.build();
	}

	private Reservation sampleReservation() {
		return Reservation.builder()
				.id(10L)
				.user(user(1L, Role.USER))
				.book(sampleBook())
				.status(ReservationStatus.ACTIVE)
				.createdAt(LocalDateTime.now())
				.build();
	}
}
