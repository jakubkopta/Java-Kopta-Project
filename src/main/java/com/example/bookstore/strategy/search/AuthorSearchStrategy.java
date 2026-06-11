package com.example.bookstore.strategy.search;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;

@Component
public class AuthorSearchStrategy implements BookSearchStrategy {

	private final BookRepository bookRepository;

	public AuthorSearchStrategy(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}

	@Override
	public BookSearchType getType() {
		return BookSearchType.AUTHOR;
	}

	@Override
	public List<Book> search(String query) {
		return bookRepository.findByAuthorContainingIgnoreCase(query);
	}
}
