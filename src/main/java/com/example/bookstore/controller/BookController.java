package com.example.bookstore.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookstore.dto.BookRequest;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.service.BookService;
import com.example.bookstore.strategy.search.BookSearchType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Book management endpoints")
public class BookController {

	private final BookService bookService;

	public BookController(BookService bookService) {
		this.bookService = bookService;
	}

	@GetMapping
	@Operation(summary = "Get all books")
	public List<BookResponse> getAllBooks() {
		return bookService.getAllBooks();
	}

	@GetMapping("/search")
	@Operation(summary = "Search books by title, author, ISBN or all fields")
	public List<BookResponse> searchBooks(
			@RequestParam(required = false) String q,
			@RequestParam(defaultValue = "ALL") BookSearchType type) {
		return bookService.searchBooks(q, type);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get book by id")
	public BookResponse getBookById(@PathVariable Long id) {
		return bookService.getBookById(id);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a new book")
	public BookResponse createBook(@Valid @RequestBody BookRequest request) {
		return bookService.createBook(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Update an existing book")
	public BookResponse updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
		return bookService.updateBook(id, request);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete a book")
	public void deleteBook(@PathVariable Long id) {
		bookService.deleteBook(id);
	}
}
