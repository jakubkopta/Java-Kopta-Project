package com.example.bookstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.bookstore.dto.BookRequest;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.exception.DuplicateResourceException;
import com.example.bookstore.exception.ResourceNotFoundException;
import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.strategy.search.BookSearchContext;
import com.example.bookstore.strategy.search.BookSearchType;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

	@Mock
	private BookRepository bookRepository;

	@Mock
	private BookSearchContext bookSearchContext;

	@InjectMocks
	private BookService bookService;

	@Test
	void getAllBooks_returnsMappedResponses() {
		Book book = sampleBook();
		when(bookRepository.findAll()).thenReturn(List.of(book));

		List<BookResponse> result = bookService.getAllBooks();

		assertThat(result).hasSize(1);
		assertThat(result.get(0).title()).isEqualTo("Test Book");
	}

	@Test
	void getBookById_whenFound_returnsBook() {
		Book book = sampleBook();
		when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

		BookResponse result = bookService.getBookById(1L);

		assertThat(result.id()).isEqualTo(1L);
	}

	@Test
	void getBookById_whenNotFound_throwsException() {
		when(bookRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> bookService.getBookById(99L))
				.isInstanceOf(ResourceNotFoundException.class);
	}

	@Test
	void searchBooks_withBlankQuery_returnsAllBooks() {
		when(bookRepository.findAll()).thenReturn(List.of(sampleBook()));

		List<BookResponse> result = bookService.searchBooks("  ", BookSearchType.TITLE);

		assertThat(result).hasSize(1);
	}

	@Test
	void searchBooks_withQuery_usesSearchContext() {
		Book book = sampleBook();
		when(bookSearchContext.search("java", BookSearchType.TITLE)).thenReturn(List.of(book));

		List<BookResponse> result = bookService.searchBooks("java", BookSearchType.TITLE);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).title()).isEqualTo("Test Book");
	}

	@Test
	void createBook_savesAndReturnsBook() {
		BookRequest request = new BookRequest("New Book", "Author", "ISBN-1", 3);
		when(bookRepository.existsByIsbn("ISBN-1")).thenReturn(false);
		when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
			Book book = invocation.getArgument(0);
			book.setId(2L);
			return book;
		});

		BookResponse result = bookService.createBook(request);

		assertThat(result.title()).isEqualTo("New Book");
		assertThat(result.availableCopies()).isEqualTo(3);
	}

	@Test
	void createBook_whenIsbnExists_throwsDuplicateException() {
		BookRequest request = new BookRequest("New Book", "Author", "ISBN-1", 3);
		when(bookRepository.existsByIsbn("ISBN-1")).thenReturn(true);

		assertThatThrownBy(() -> bookService.createBook(request))
				.isInstanceOf(DuplicateResourceException.class);
	}

	@Test
	void updateBook_updatesAvailableCopiesCorrectly() {
		Book book = sampleBook();
		book.setAvailableCopies(3);
		BookRequest request = new BookRequest("Updated", "Author", "ISBN-1", 5);
		when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
		when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

		BookResponse result = bookService.updateBook(1L, request);

		assertThat(result.title()).isEqualTo("Updated");
		assertThat(result.availableCopies()).isEqualTo(3);
	}

	@Test
	void updateBook_whenTotalCopiesTooLow_throwsException() {
		Book book = sampleBook();
		book.setTotalCopies(5);
		book.setAvailableCopies(2);
		BookRequest request = new BookRequest("Updated", "Author", "ISBN-1", 2);
		when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

		assertThatThrownBy(() -> bookService.updateBook(1L, request))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void deleteBook_deletesExistingBook() {
		Book book = sampleBook();
		when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

		bookService.deleteBook(1L);

		verify(bookRepository).delete(book);
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
}
