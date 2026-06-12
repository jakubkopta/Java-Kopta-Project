package com.example.bookstore.strategy.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;

@ExtendWith(MockitoExtension.class)
class SearchStrategiesTest {

	@Mock
	private BookRepository bookRepository;

	@Test
	void titleSearchStrategy_returnsBooks() {
		TitleSearchStrategy strategy = new TitleSearchStrategy(bookRepository);
		when(bookRepository.findByTitleContainingIgnoreCase("java")).thenReturn(List.of(Book.builder().title("Java").build()));

		assertThat(strategy.getType()).isEqualTo(BookSearchType.TITLE);
		assertThat(strategy.search("java")).hasSize(1);
	}

	@Test
	void authorSearchStrategy_returnsBooks() {
		AuthorSearchStrategy strategy = new AuthorSearchStrategy(bookRepository);
		when(bookRepository.findByAuthorContainingIgnoreCase("author")).thenReturn(List.of());

		assertThat(strategy.getType()).isEqualTo(BookSearchType.AUTHOR);
		assertThat(strategy.search("author")).isEmpty();
	}

	@Test
	void isbnSearchStrategy_returnsBooks() {
		IsbnSearchStrategy strategy = new IsbnSearchStrategy(bookRepository);
		when(bookRepository.findByIsbnContainingIgnoreCase("978")).thenReturn(List.of());

		assertThat(strategy.getType()).isEqualTo(BookSearchType.ISBN);
		assertThat(strategy.search("978")).isEmpty();
	}

	@Test
	void allFieldsSearchStrategy_returnsBooks() {
		AllFieldsSearchStrategy strategy = new AllFieldsSearchStrategy(bookRepository);
		when(bookRepository.searchAllFields("book")).thenReturn(List.of());

		assertThat(strategy.getType()).isEqualTo(BookSearchType.ALL);
		assertThat(strategy.search("book")).isEmpty();
	}
}
