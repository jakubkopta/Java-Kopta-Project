package com.example.bookstore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bookstore.model.Reservation;
import com.example.bookstore.model.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	List<Reservation> findByUserId(Long userId);

	List<Reservation> findByBookId(Long bookId);

	List<Reservation> findByUserIdAndStatus(Long userId, ReservationStatus status);
}
