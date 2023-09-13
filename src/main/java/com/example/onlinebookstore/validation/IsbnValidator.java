package com.example.onlinebookstore.validation;

import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.repository.book.BookRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

public class IsbnValidator implements ConstraintValidator<Isbn, String> {
    @Autowired
    private BookRepository bookRepository;

    @Override
    public boolean isValid(String isbn, ConstraintValidatorContext constraintValidatorContext) {
        List<String> listOfIsbn = bookRepository.findAll().stream()
                .map(Book::getIsbn)
                .toList();
        return !listOfIsbn.contains(isbn);
    }
}
