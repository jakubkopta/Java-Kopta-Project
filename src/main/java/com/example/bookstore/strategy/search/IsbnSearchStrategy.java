package com.example.bookstore.strategy.search;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;

@Component
public class IsbnSearchStrategy implements BookSearchStrategy {

	private final BookRepository bookRepository;

	public IsbnSearchStrategy(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}

	@Override
	public BookSearchType getType() {
		return BookSearchType.ISBN;
	}

	@Override
	public List<Book> search(String query) {
		return bookRepository.findByIsbnContainingIgnoreCase(query);
	}
}
