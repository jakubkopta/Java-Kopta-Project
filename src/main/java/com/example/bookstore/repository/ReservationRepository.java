package com.example.bookstore.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bookstore.model.Reservation;
import com.example.bookstore.model.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);

	List<Reservation> findAllByOrderByCreatedAtDesc();

	List<Reservation> findByBookId(Long bookId);

	boolean existsByUserIdAndBookIdAndStatusIn(Long userId, Long bookId, Collection<ReservationStatus> statuses);
}
