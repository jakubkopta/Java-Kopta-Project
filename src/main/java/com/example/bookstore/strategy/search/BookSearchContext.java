package com.example.bookstore.strategy.search;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.bookstore.model.Book;

@Service
public class BookSearchContext {

	private final Map<BookSearchType, BookSearchStrategy> strategies;

	public BookSearchContext(List<BookSearchStrategy> strategyList) {
		this.strategies = strategyList.stream()
				.collect(Collectors.toMap(BookSearchStrategy::getType, Function.identity()));
	}

	public List<Book> search(String query, BookSearchType type) {
		BookSearchStrategy strategy = strategies.get(type);

		if (strategy == null) {
			throw new IllegalArgumentException("Unsupported search type: " + type);
		}

		return strategy.search(query);
	}
}
