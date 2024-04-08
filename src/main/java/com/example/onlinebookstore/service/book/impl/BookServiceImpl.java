package com.example.onlinebookstore.service.book.impl;

import com.example.onlinebookstore.dto.book.BookDto;
import com.example.onlinebookstore.dto.book.BookDtoWithoutCategoryIds;
import com.example.onlinebookstore.dto.book.BookSearchParametersDto;
import com.example.onlinebookstore.dto.book.CreateBookRequestDto;
import com.example.onlinebookstore.exception.EntityNotFoundException;
import com.example.onlinebookstore.mapper.BookMapper;
import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.model.Category;
import com.example.onlinebookstore.repository.book.BookRepository;
import com.example.onlinebookstore.repository.book.BookSpecificationBuilder;
import com.example.onlinebookstore.repository.category.CategoryRepository;
import com.example.onlinebookstore.service.book.BookService;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
    private final CategoryRepository categoryRepository;

    @Override
    public BookDto save(CreateBookRequestDto request) {
        Book model = bookMapper.toModel(request);
        if (request.getCategoryIds() != null) {
            getCategoriesByIds(request.getCategoryIds())
                    .forEach(category -> category.addBook(model));
        }
        return bookMapper.toDto(bookRepository.save(model));
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
        return bookRepository.findById(id).map(bookMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("There is no Book with id: " + id));
    }

    @Override
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public BookDto update(Long id, CreateBookRequestDto request) {
        Optional<Book> optionalBook = bookRepository.findById(id);
        if (optionalBook.isPresent()) {
            Book updatedBook = bookMapper
                    .updateBookModelFromBookDto(optionalBook.get(), request);
            updatedBook.setId(id);
            if (request.getCategoryIds() != null) {
                getCategoriesByIds(request.getCategoryIds())
                        .forEach(category -> category.addBook(updatedBook));
            }
            return bookMapper.toDto(bookRepository.save(updatedBook));
        }
        throw new EntityNotFoundException("There is no book with id: " + id);
    }

    @Override
    public List<BookDto> searchBooks(BookSearchParametersDto searchParameters) {
        Specification<Book> specification = builder.build(searchParameters);
        return bookRepository.findAll(specification)
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public List<BookDtoWithoutCategoryIds> findAllBooksByCategoryIds(
            Long categoryId, Pageable pageable
    ) {
        return bookRepository.findAllByCategoryIds(categoryId, pageable)
                .stream()
                .map(bookMapper::toDtoWithoutCategoryIds)
                .toList();
    }

    private Set<Category> getCategoriesByIds(Collection<Long> ids) {
        return ids.stream()
                .map(categoryRepository::findById)
                .map(category -> category.orElseThrow(() ->
                        new EntityNotFoundException("Category wasn't found")))
                .collect(Collectors.toSet());
    }
}
