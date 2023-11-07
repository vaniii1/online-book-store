package com.example.onlinebookstore.repository;

import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.repository.book.BookRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest {
    private static final Long ID_ONE = 1L;
    @Autowired
    private BookRepository bookRepository;

    @Test
    @Sql(scripts = {"classpath:database/book/add-five-books.sql",
            "classpath:database/category/add-three-categories.sql",
            "classpath:database/book/add-categories-to-books.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/book/delete-book-category-connection.sql",
            "classpath:database/book/delete-books.sql",
            "classpath:database/category/delete-categories.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("""
            Find all books by valid category id
            """)
    void findAllByCategoryId_CategoryIdIsValid_NotEmptyList() {
        Page<Book> actual = bookRepository.findAllByCategoryIds(
                ID_ONE, PageRequest.of(0, 5));
        Assertions.assertEquals(3, actual.toList().size());
        Assertions.assertEquals("Snow White", actual.toList().get(0).getTitle());
        Assertions.assertEquals("Sherlock Holmes", actual.toList().get(1).getTitle());
        Assertions.assertEquals("Fairy tail", actual.toList().get(2).getTitle());
    }
}
