package com.example.bookstore.strategy.search;

import java.util.List;

import com.example.bookstore.model.Book;

public interface BookSearchStrategy {

	BookSearchType getType();

	List<Book> search(String query);
}
