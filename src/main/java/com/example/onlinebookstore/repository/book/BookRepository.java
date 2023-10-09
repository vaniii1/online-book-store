package com.example.onlinebookstore.repository.book;

import com.example.onlinebookstore.model.Book;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    @Query(value = "SELECT * FROM books b INNER JOIN books_categories bc ON "
            + "b.id = bc.book_id WHERE bc.category_id = :categoryId", nativeQuery = true)
    List<Book> findAllByCategoryIds(Long categoryId, Pageable pageable);

    Optional<Book> findBookById(Long id);
}

