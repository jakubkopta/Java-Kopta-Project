package com.example.bookstore.strategy.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.bookstore.model.Book;

@ExtendWith(MockitoExtension.class)
class BookSearchContextTest {

	@Mock
	private TitleSearchStrategy titleSearchStrategy;

	@Mock
	private AuthorSearchStrategy authorSearchStrategy;

	@Mock
	private IsbnSearchStrategy isbnSearchStrategy;

	@Mock
	private AllFieldsSearchStrategy allFieldsSearchStrategy;

	private BookSearchContext bookSearchContext;

	@BeforeEach
	void setUp() {
		when(titleSearchStrategy.getType()).thenReturn(BookSearchType.TITLE);
		when(authorSearchStrategy.getType()).thenReturn(BookSearchType.AUTHOR);
		when(isbnSearchStrategy.getType()).thenReturn(BookSearchType.ISBN);
		when(allFieldsSearchStrategy.getType()).thenReturn(BookSearchType.ALL);

		bookSearchContext = new BookSearchContext(List.of(
				titleSearchStrategy,
				authorSearchStrategy,
				isbnSearchStrategy,
				allFieldsSearchStrategy));
	}

	@Test
	void search_usesTitleStrategy() {
		Book book = Book.builder().title("Java").build();
		when(titleSearchStrategy.search("java")).thenReturn(List.of(book));

		List<Book> result = bookSearchContext.search("java", BookSearchType.TITLE);

		assertThat(result).hasSize(1);
	}

	@Test
	void search_usesAllFieldsStrategy() {
		when(allFieldsSearchStrategy.search("book")).thenReturn(List.of());

		List<Book> result = bookSearchContext.search("book", BookSearchType.ALL);

		assertThat(result).isEmpty();
	}

	@Test
	void search_withUnsupportedType_throwsException() {
		assertThatThrownBy(() -> bookSearchContext.search("x", null))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
