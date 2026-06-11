package com.example.bookstore.strategy.search;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;

@Component
public class AllFieldsSearchStrategy implements BookSearchStrategy {

	private final BookRepository bookRepository;

	public AllFieldsSearchStrategy(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}

	@Override
	public BookSearchType getType() {
		return BookSearchType.ALL;
	}

	@Override
	public List<Book> search(String query) {
		return bookRepository.searchAllFields(query);
	}
}
