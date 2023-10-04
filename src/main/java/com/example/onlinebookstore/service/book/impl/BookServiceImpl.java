package com.example.onlinebookstore.service.book.impl;

import com.example.onlinebookstore.dto.book.BookDto;
import com.example.onlinebookstore.dto.book.BookSearchParametersDto;
import com.example.onlinebookstore.dto.book.CreateBookRequestDto;
import com.example.onlinebookstore.exception.EntityNotFoundException;
import com.example.onlinebookstore.mapper.BookMapper;
import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.repository.book.BookRepository;
import com.example.onlinebookstore.repository.book.BookSpecificationBuilder;
import com.example.onlinebookstore.service.book.BookService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookSpecificationBuilder builder;

    @Override
    public BookDto save(CreateBookRequestDto requestDto) {
        Book savedBook = bookRepository.save(bookMapper.toModel(requestDto));
        return bookMapper.toDto(savedBook);
    }

    @Override
    public List<BookDto> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable)
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public BookDto getBookById(Long id) {
        return bookRepository.findBookById(id)
                .map(bookMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("There is no book with id: " + id));
    }

    @Override
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public void update(Long id, CreateBookRequestDto requestDto) {
        Optional<Book> optionalBook = bookRepository.findBookById(id);
        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            book.setTitle(requestDto.getTitle());
            book.setAuthor(requestDto.getAuthor());
            book.setIsbn(requestDto.getIsbn());
            book.setPrice(requestDto.getPrice());
            book.setDescription(requestDto.getDescription());
            book.setCoverImage(requestDto.getCoverImage());
            bookRepository.save(book);
        } else {
            throw new EntityNotFoundException("There is no book with id: " + id);
        }
    }

    @Override
    public List<BookDto> searchBooks(BookSearchParametersDto searchParameters) {
        Specification<Book> specification = builder.build(searchParameters);
        return bookRepository.findAll(specification)
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }

}
