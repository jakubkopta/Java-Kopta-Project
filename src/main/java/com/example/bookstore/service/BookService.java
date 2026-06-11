package com.example.bookstore.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookstore.dto.BookRequest;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.exception.DuplicateResourceException;
import com.example.bookstore.exception.ResourceNotFoundException;
import com.example.bookstore.model.Book;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.strategy.search.BookSearchContext;
import com.example.bookstore.strategy.search.BookSearchType;

@Service
@Transactional
public class BookService {

	private final BookRepository bookRepository;
	private final BookSearchContext bookSearchContext;

	public BookService(BookRepository bookRepository, BookSearchContext bookSearchContext) {
		this.bookRepository = bookRepository;
		this.bookSearchContext = bookSearchContext;
	}

	@Transactional(readOnly = true)
	public List<BookResponse> getAllBooks() {
		return bookRepository.findAll().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public BookResponse getBookById(Long id) {
		return toResponse(findBookOrThrow(id));
	}

	@Transactional(readOnly = true)
	public List<BookResponse> searchBooks(String query, BookSearchType type) {
		if (query == null || query.isBlank()) {
			return getAllBooks();
		}

		BookSearchType searchType = type != null ? type : BookSearchType.ALL;

		return bookSearchContext.search(query.trim(), searchType).stream()
				.map(this::toResponse)
				.toList();
	}

	public BookResponse createBook(BookRequest request) {
		if (bookRepository.existsByIsbn(request.isbn())) {
			throw new DuplicateResourceException("Book with ISBN " + request.isbn() + " already exists");
		}

		Book book = Book.builder()
				.title(request.title())
				.author(request.author())
				.isbn(request.isbn())
				.totalCopies(request.totalCopies())
				.availableCopies(request.totalCopies())
				.createdAt(LocalDateTime.now())
				.build();

		return toResponse(bookRepository.save(book));
	}

	public BookResponse updateBook(Long id, BookRequest request) {
		Book book = findBookOrThrow(id);

		if (!book.getIsbn().equals(request.isbn()) && bookRepository.existsByIsbn(request.isbn())) {
			throw new DuplicateResourceException("Book with ISBN " + request.isbn() + " already exists");
		}

		int borrowedCopies = book.getTotalCopies() - book.getAvailableCopies();
		if (request.totalCopies() < borrowedCopies) {
			throw new IllegalArgumentException(
					"Total copies cannot be less than currently borrowed copies (" + borrowedCopies + ")");
		}

		book.setTitle(request.title());
		book.setAuthor(request.author());
		book.setIsbn(request.isbn());
		book.setTotalCopies(request.totalCopies());
		book.setAvailableCopies(request.totalCopies() - borrowedCopies);

		return toResponse(bookRepository.save(book));
	}

	public void deleteBook(Long id) {
		Book book = findBookOrThrow(id);
		bookRepository.delete(book);
	}

	private Book findBookOrThrow(Long id) {
		return bookRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
	}

	private BookResponse toResponse(Book book) {
		return new BookResponse(
				book.getId(),
				book.getTitle(),
				book.getAuthor(),
				book.getIsbn(),
				book.getTotalCopies(),
				book.getAvailableCopies(),
				book.getCreatedAt()
		);
	}
}
