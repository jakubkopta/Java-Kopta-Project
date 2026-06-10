package com.example.bookstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.bookstore.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

	Optional<Book> findByIsbn(String isbn);

	List<Book> findByTitleContainingIgnoreCase(String title);

	List<Book> findByAuthorContainingIgnoreCase(String author);

	boolean existsByIsbn(String isbn);

	@Query("""
			SELECT b FROM Book b
			WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
			   OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))
			   OR b.isbn LIKE CONCAT('%', :query, '%')
			""")
	List<Book> search(@Param("query") String query);
}
