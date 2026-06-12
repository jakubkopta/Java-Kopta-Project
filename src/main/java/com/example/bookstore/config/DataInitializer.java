package com.example.bookstore.config;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.bookstore.model.Book;
import com.example.bookstore.model.Role;
import com.example.bookstore.model.User;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.UserRepository;

@Component
public class DataInitializer implements ApplicationRunner {

	private static final String ADMIN_EMAIL = "admin@bookstore.com";
	private static final String ADMIN_PASSWORD = "admin123";
	private static final String DEMO_USER_EMAIL = "user@example.com";
	private static final String DEMO_USER_PASSWORD = "user123";

	private final UserRepository userRepository;
	private final BookRepository bookRepository;
	private final PasswordEncoder passwordEncoder;

	public DataInitializer(
			UserRepository userRepository,
			BookRepository bookRepository,
			PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.bookRepository = bookRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		ensureAdmin();
		ensureDemoUser();
		if (bookRepository.count() > 0) {
			return;
		}
		seedBooks(LocalDateTime.now());
	}

	private void ensureAdmin() {
		if (!userRepository.existsByEmail(ADMIN_EMAIL)) {
			userRepository.save(User.builder()
					.email(ADMIN_EMAIL)
					.password(passwordEncoder.encode(ADMIN_PASSWORD))
					.role(Role.ADMIN)
					.createdAt(LocalDateTime.now())
					.build());
		}
	}

	private void ensureDemoUser() {
		if (!userRepository.existsByEmail(DEMO_USER_EMAIL)) {
			userRepository.save(User.builder()
					.email(DEMO_USER_EMAIL)
					.password(passwordEncoder.encode(DEMO_USER_PASSWORD))
					.role(Role.USER)
					.createdAt(LocalDateTime.now())
					.build());
		}
	}

	private void seedBooks(LocalDateTime createdAt) {
		List<Book> books = List.of(
				book("Clean Code", "Robert C. Martin", "9780132350884", 5, createdAt),
				book("Effective Java", "Joshua Bloch", "9780134685991", 4, createdAt),
				book("The Hobbit", "J.R.R. Tolkien", "9780547928227", 3, createdAt),
				book("1984", "George Orwell", "9780451524935", 2, createdAt),
				book("Dune", "Frank Herbert", "9780441172719", 3, createdAt),
				book("Spring in Action", "Craig Walls", "9781617297571", 2, createdAt),
				book("Design Patterns", "Erich Gamma", "9780201633610", 2, createdAt),
				book("The Pragmatic Programmer", "David Thomas", "9780135957059", 3, createdAt),
				book("Domain-Driven Design", "Eric Evans", "9780321125217", 2, createdAt),
				book("Harry Potter and the Philosopher's Stone", "J.K. Rowling", "9780747532699", 6, createdAt),
				book("The Lord of the Rings", "J.R.R. Tolkien", "9780544003415", 4, createdAt),
				book("Crime and Punishment", "Fyodor Dostoevsky", "9780143058144", 2, createdAt),
				book("The Great Gatsby", "F. Scott Fitzgerald", "9780743273565", 3, createdAt),
				book("To Kill a Mockingbird", "Harper Lee", "9780061120084", 3, createdAt),
				book("Brave New World", "Aldous Huxley", "9780060850524", 2, createdAt),
				book("The Catcher in the Rye", "J.D. Salinger", "9780316769488", 2, createdAt),
				book("Sapiens", "Yuval Noah Harari", "9780062316097", 4, createdAt),
				book("Atomic Habits", "James Clear", "9780735211292", 5, createdAt),
				book("The Shining", "Stephen King", "9780307743657", 3, createdAt),
				book("Norwegian Wood", "Haruki Murakami", "9780375704024", 2, createdAt));

		bookRepository.saveAll(books);
	}

	private Book book(String title, String author, String isbn, int copies, LocalDateTime createdAt) {
		return Book.builder()
				.title(title)
				.author(author)
				.isbn(isbn)
				.totalCopies(copies)
				.availableCopies(copies)
				.createdAt(createdAt)
				.build();
	}
}
