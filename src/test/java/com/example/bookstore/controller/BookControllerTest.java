package com.example.bookstore.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.bookstore.config.SecurityConfig;
import com.example.bookstore.dto.BookResponse;
import com.example.bookstore.security.CustomUserDetailsService;
import com.example.bookstore.security.JwtService;
import com.example.bookstore.service.BookService;
import com.example.bookstore.strategy.search.BookSearchType;

@WebMvcTest(BookController.class)
@Import({SecurityConfig.class, com.example.bookstore.exception.GlobalExceptionHandler.class})
class BookControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BookService bookService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private CustomUserDetailsService customUserDetailsService;

	@Test
	void getAllBooks_returnsOk() throws Exception {
		when(bookService.getAllBooks()).thenReturn(List.of(
				new BookResponse(1L, "Book", "Author", "ISBN", 5, 5, LocalDateTime.now())));

		mockMvc.perform(get("/api/books"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title").value("Book"));
	}

	@Test
	void searchBooks_returnsOk() throws Exception {
		when(bookService.searchBooks("java", BookSearchType.TITLE)).thenReturn(List.of());

		mockMvc.perform(get("/api/books/search").param("q", "java").param("type", "TITLE"))
				.andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void createBook_asAdmin_returnsCreated() throws Exception {
		when(bookService.createBook(any())).thenReturn(
				new BookResponse(1L, "Book", "Author", "ISBN", 5, 5, LocalDateTime.now()));

		mockMvc.perform(post("/api/books")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Book",
								  "author": "Author",
								  "isbn": "ISBN",
								  "totalCopies": 5
								}
								"""))
				.andExpect(status().isCreated());
	}

	@Test
	@WithMockUser(roles = "USER")
	void createBook_asUser_returnsForbidden() throws Exception {
		mockMvc.perform(post("/api/books")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "title": "Book",
								  "author": "Author",
								  "isbn": "ISBN",
								  "totalCopies": 5
								}
								"""))
				.andExpect(status().isForbidden());
	}
}
