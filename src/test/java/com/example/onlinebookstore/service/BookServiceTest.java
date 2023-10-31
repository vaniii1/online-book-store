package com.example.onlinebookstore.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.onlinebookstore.dto.book.BookDto;
import com.example.onlinebookstore.dto.book.BookDtoWithoutCategoryIds;
import com.example.onlinebookstore.dto.book.BookSearchParametersDto;
import com.example.onlinebookstore.dto.book.CreateBookRequestDto;
import com.example.onlinebookstore.exception.EntityNotFoundException;
import com.example.onlinebookstore.mapper.BookMapper;
import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.model.Category;
import com.example.onlinebookstore.repository.BookRepository;
import com.example.onlinebookstore.repository.BookSpecificationBuilder;
import com.example.onlinebookstore.repository.category.CategoryRepository;
import com.example.onlinebookstore.service.book.impl.BookServiceImpl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
    private static final Long VALID_ID_ONE = 1L;
    private static final Long VALID_ID_TWO = 2L;
    private static final Long VALID_ID_FOUR = 4L;
    private static final Long INVALID_ID_FIVE = 5L;

    private static CreateBookRequestDto firstRequest;
    private static Book firstBook;
    private static BookDto firstExpected;
    private static CreateBookRequestDto secondRequest;
    private static CreateBookRequestDto updateRequest;
    private static Book secondBook;
    private static BookDto secondExpected;
    private static Category category;

    @Mock
    private BookRepository bookRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private BookSpecificationBuilder builder;
    @InjectMocks
    private BookServiceImpl bookService;

    @BeforeAll
    static void beforeAll() {
        category = new Category()
                .setName("category sample 1");

        firstRequest = new CreateBookRequestDto()
                .setAuthor("sample author 1")
                .setTitle("sample title 1")
                .setPrice(BigDecimal.valueOf(44.4))
                .setIsbn("136136161461")
                .setCategoryIds(new HashSet<>(Set.of(VALID_ID_ONE)));

        firstBook = new Book()
                .setAuthor(firstRequest.getAuthor())
                .setTitle(firstRequest.getTitle())
                .setPrice(firstRequest.getPrice())
                .setIsbn(firstRequest.getIsbn());

        firstExpected = new BookDto()
                .setId(VALID_ID_ONE)
                .setAuthor(firstBook.getAuthor())
                .setTitle(firstBook.getTitle())
                .setPrice(firstBook.getPrice())
                .setIsbn(firstBook.getIsbn())
                .setCategoryIds(new HashSet<>(Set.of(VALID_ID_ONE)));

        secondRequest = new CreateBookRequestDto()
                .setAuthor("sample author 2")
                .setTitle("sample title 2")
                .setPrice(BigDecimal.valueOf(44.4))
                .setIsbn("326246427247")
                .setCategoryIds(new HashSet<>(Set.of(VALID_ID_ONE)));

        secondBook = new Book()
                .setAuthor(secondRequest.getAuthor())
                .setTitle(secondRequest.getTitle())
                .setPrice(secondRequest.getPrice())
                .setIsbn(secondRequest.getIsbn());

        secondExpected = new BookDto()
                .setId(VALID_ID_TWO)
                .setAuthor(secondBook.getAuthor())
                .setTitle(secondBook.getTitle())
                .setPrice(secondBook.getPrice())
                .setIsbn(secondBook.getIsbn())
                .setCategoryIds(new HashSet<>(Set.of(VALID_ID_ONE)));

        updateRequest = new CreateBookRequestDto()
                .setAuthor("update author")
                .setTitle("update title")
                .setPrice(BigDecimal.valueOf(55.5))
                .setIsbn("1513515135")
                .setCategoryIds(new HashSet<>(Set.of(VALID_ID_ONE)));
    }

    @Test
    @DisplayName("""
            Must save valid book  
            """)
    void save_ValidRequestDto_ReturnBookDto() {
        Mockito.when(bookMapper.toModel(firstRequest)).thenReturn(firstBook);
        Mockito.when(bookRepository.save(firstBook)).thenReturn(firstBook);
        Mockito.when(bookMapper.toDto(firstBook)).thenReturn(firstExpected);
        Mockito.when(categoryRepository.findById(VALID_ID_ONE)).thenReturn(Optional.of(category));

        BookDto actual = bookService.save(firstRequest);

        assertThat(actual).isEqualTo(firstExpected);
        Mockito.verify(bookMapper, Mockito.times(1)).toModel(firstRequest);
        Mockito.verify(bookRepository, Mockito.times(1)).save(firstBook);
        Mockito.verify(categoryRepository, Mockito.times(1)).findById(VALID_ID_ONE);
        Mockito.verify(bookMapper, Mockito.times(1)).toDto(firstBook);
        Mockito.verifyNoMoreInteractions(bookRepository, bookMapper, categoryRepository);
    }

    @Test
    @DisplayName("""
            Must return two books  
            """)
    void findAll_TwoBooks_Ok() {
        Pageable pageable = PageRequest.of(0, 5);
        List<Book> books = new ArrayList<>(List.of(firstBook, secondBook));
        Page<Book> page = new PageImpl<>(books, pageable, books.size());

        Mockito.when(bookRepository.findAll(pageable)).thenReturn(page);
        Mockito.when(bookMapper.toDto(firstBook)).thenReturn(firstExpected);
        Mockito.when(bookMapper.toDto(secondBook)).thenReturn(secondExpected);

        List<BookDto> expected = new ArrayList<>(List.of(firstExpected, secondExpected));
        List<BookDto> actual = bookService.findAll(pageable);

        assertThat(actual).isNotNull();
        assertThat(actual).hasSize(2);
        assertThat(actual).isEqualTo(expected);

        Mockito.verify(bookRepository, Mockito.times(1)).findAll(pageable);
        Mockito.verify(bookMapper, Mockito.times(1)).toDto(firstBook);
        Mockito.verify(bookMapper, Mockito.times(1)).toDto(secondBook);
        Mockito.verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("""
            Must return firstBook with id 1L
            """)
    void getBookById_ValidId_ReturnFirstBook() {
        Mockito.when(bookRepository.findById(VALID_ID_ONE)).thenReturn(Optional.of(firstBook));
        Mockito.when(bookMapper.toDto(firstBook)).thenReturn(firstExpected);

        BookDto actual = bookService.getBookById(VALID_ID_ONE);
        assertThat(actual).isEqualTo(firstExpected);

        Mockito.verify(bookRepository, Mockito.times(1)).findById(VALID_ID_ONE);
        Mockito.verify(bookMapper, Mockito.times(1)).toDto(firstBook);
        Mockito.verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("""
            Must delete an entity with valid Id
            """)
    void deleteById_ValidId_Ok() {
        bookService.deleteById(VALID_ID_FOUR);
        Mockito.verify(bookRepository,
                Mockito.times(1)).deleteById(VALID_ID_FOUR);
        Mockito.verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("""
            Must update an entity with valid input
            """)
    void update_ValidRequest_Ok() {
        Book updateBook = new Book()
                .setAuthor(updateRequest.getAuthor())
                .setTitle(updateRequest.getTitle())
                .setIsbn(updateRequest.getIsbn())
                .setPrice(updateRequest.getPrice());

        BookDto updateExpect = new BookDto()
                .setId(VALID_ID_ONE)
                .setAuthor(updateBook.getAuthor())
                .setTitle(updateBook.getTitle())
                .setIsbn(updateBook.getIsbn())
                .setPrice(updateBook.getPrice())
                .setCategoryIds(new HashSet<>(Set.of(VALID_ID_ONE)));

        Mockito.when(bookRepository.findById(VALID_ID_ONE)).thenReturn(Optional.of(firstBook));
        Mockito.when(bookMapper.toModel(updateRequest)).thenReturn(updateBook);
        Mockito.when(bookRepository.save(updateBook)).thenReturn(updateBook);
        Mockito.when(bookMapper.toDto(updateBook)).thenReturn(updateExpect);
        Mockito.when(categoryRepository.findById(VALID_ID_ONE)).thenReturn(Optional.of(category));

        BookDto actual = bookService.update(VALID_ID_ONE, updateRequest);

        assertThat(actual).isEqualTo(updateExpect);
        Mockito.verify(bookRepository, Mockito.times(1)).findById(VALID_ID_ONE);
        Mockito.verify(bookRepository, Mockito.times(1)).save(updateBook);
        Mockito.verify(bookMapper, Mockito.times(1)).toModel(updateRequest);
        Mockito.verify(bookMapper, Mockito.times(1)).toDto(updateBook);
        Mockito.verify(categoryRepository, Mockito.times(1)).findById(VALID_ID_ONE);
        Mockito.verifyNoMoreInteractions(bookRepository, bookMapper, categoryRepository);
    }

    @Test
    @DisplayName("""
            Must return firstBook with search params
            """)
    void searchBooks_ParamTitle_Ok() {
        BookSearchParametersDto params = new BookSearchParametersDto(
                new String[]{"sample title 1"},
                new String[]{},
                new String[]{},
                new String[]{}
        );
        Specification<Book> specification = Specification.where((root, query, criteriaBuilder) ->
                root.get("title").in(params));
        List<Book> books = new ArrayList<>(List.of(firstBook));

        Mockito.when(builder.build(params)).thenReturn(specification);
        Mockito.when(bookRepository.findAll(specification)).thenReturn(books);
        Mockito.when(bookMapper.toDto(books.get(0))).thenReturn(firstExpected);

        List<BookDto> expected = new ArrayList<>(List.of(firstExpected));
        List<BookDto> actual = bookService.searchBooks(params);

        assertThat(actual.size()).isEqualTo(expected.size());
        assertThat(actual).isEqualTo(expected);

        Mockito.verify(builder, Mockito.times(1)).build(params);
        Mockito.verify(bookRepository, Mockito.times(1)).findAll(specification);
        Mockito.verify(bookMapper, Mockito.times(1)).toDto(books.get(0));
        Mockito.verifyNoMoreInteractions(builder, bookRepository, bookMapper);
    }

    @Test
    @DisplayName("""
            Must return two Books with valid Category Id
            """)
    void findAllBooksByCategoryIds_ValidInput_Ok() {
        Pageable pageable = PageRequest.of(0, 5);
        List<Book> books = new ArrayList<>(List.of(firstBook, secondBook));
        Page<Book> page = new PageImpl<>(books, pageable, books.size());

        BookDtoWithoutCategoryIds firstWithoutCategoriesExpected = new BookDtoWithoutCategoryIds()
                .setAuthor(firstBook.getAuthor())
                .setTitle(firstBook.getTitle())
                .setIsbn(firstBook.getIsbn())
                .setPrice(firstBook.getPrice());

        BookDtoWithoutCategoryIds secondWithoutCategoriesExpected = new BookDtoWithoutCategoryIds()
                .setAuthor(secondBook.getAuthor())
                .setTitle(secondBook.getTitle())
                .setIsbn(secondBook.getIsbn())
                .setPrice(secondBook.getPrice());

        Mockito.when(bookRepository
                .findAllByCategoryIds(category.getId(), pageable))
                .thenReturn(page);
        Mockito.when(bookMapper.toDtoWithoutCategoryIds(firstBook))
                .thenReturn(firstWithoutCategoriesExpected);
        Mockito.when(bookMapper.toDtoWithoutCategoryIds(secondBook))
                .thenReturn(secondWithoutCategoriesExpected);

        List<BookDtoWithoutCategoryIds> expected = new ArrayList<>(
                List.of(firstWithoutCategoriesExpected,
                        secondWithoutCategoriesExpected)
        );
        List<BookDtoWithoutCategoryIds> actual =
                bookService.findAllBooksByCategoryIds(category.getId(), pageable);

        assertThat(actual.get(0).getAuthor()).isEqualTo(expected.get(0).getAuthor());
        assertThat(actual.get(1).getAuthor()).isEqualTo(expected.get(1).getAuthor());
        assertThat(actual).isEqualTo(expected);
        Mockito.verify(bookRepository, Mockito.times(1))
                .findAllByCategoryIds(category.getId(), pageable);
        Mockito.verify(bookMapper, Mockito.times(1))
                .toDtoWithoutCategoryIds(firstBook);
        Mockito.verify(bookMapper, Mockito.times(1))
                .toDtoWithoutCategoryIds(secondBook);
        Mockito.verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("""
            Must throw exception with invalid Id
            """)
    void getBookById_InvalidId_ThrowsEntityNotFoundException() {
        Mockito.when(bookRepository.findById(INVALID_ID_FIVE)).thenReturn(Optional.empty());

        Exception exception = Assert.assertThrows(
                EntityNotFoundException.class,
                () -> bookService.getBookById(INVALID_ID_FIVE)
        );

        String expected = "There is no Book with id: " + INVALID_ID_FIVE;
        String actual = exception.getMessage();

        assertThat(actual).isEqualTo(expected);

        Mockito.verify(bookRepository, Mockito.times(1)).findById(INVALID_ID_FIVE);
        Mockito.verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("""
            Must throw exception with invalid Id
            """)
    void getBookById_InvalidId_ThrowsException() {
        Mockito.when(bookRepository.findById(INVALID_ID_FIVE)).thenReturn(Optional.empty());
        Exception exception = Assert.assertThrows(
                EntityNotFoundException.class,
                () -> bookService.getBookById(INVALID_ID_FIVE)
        );
        String expected = "There is no Book with id: " + INVALID_ID_FIVE;
        String actual = exception.getMessage();

        assertThat(actual).isEqualTo(expected);

        Mockito.verify(bookRepository, Mockito.times(1)).findById(INVALID_ID_FIVE);
        Mockito.verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("""
            Must throw exception with invalid Category Id while saving entity
            """)
    void save_InvalidCategoryIdInRequest_ThrowsException() {

        CreateBookRequestDto invalidRequest = new CreateBookRequestDto()
                .setAuthor("author sample 3")
                .setTitle("title sample 3")
                .setIsbn("21513616146164")
                .setPrice(BigDecimal.valueOf(24.4))
                .setCategoryIds(new HashSet<>(Set.of(INVALID_ID_FIVE)));

        Book book = new Book()
                .setAuthor(invalidRequest.getAuthor())
                .setTitle(invalidRequest.getTitle())
                .setIsbn(invalidRequest.getIsbn())
                .setPrice(invalidRequest.getPrice());

        Mockito.when(bookMapper.toModel(invalidRequest)).thenReturn(book);
        Mockito.when(categoryRepository.findById(INVALID_ID_FIVE)).thenReturn(Optional.empty());

        Exception exception = Assert.assertThrows(
                EntityNotFoundException.class,
                () -> bookService.save(invalidRequest)
        );

        String expected = "Category wasn't found";
        String actual = exception.getMessage();

        assertThat(actual).isEqualTo(expected);

        Mockito.verify(bookMapper, Mockito.times(1)).toModel(invalidRequest);
        Mockito.verify(categoryRepository, Mockito.times(1)).findById(INVALID_ID_FIVE);
        Mockito.verifyNoMoreInteractions(bookMapper, bookRepository, categoryRepository);
    }

    @Test
    @DisplayName("""
            Must throw an Exception with invalid Book Id calling update method
            """)
    void update_InvalidBookId_ThrowsException() {
        Mockito.when(bookRepository.findById(INVALID_ID_FIVE)).thenReturn(Optional.empty());

        Exception exception = Assert.assertThrows(
                EntityNotFoundException.class,
                () -> bookService.update(INVALID_ID_FIVE, updateRequest)
        );

        String expected = "There is no book with id: " + INVALID_ID_FIVE;
        String actual = exception.getMessage();

        assertThat(actual).isEqualTo(expected);
        Mockito.verify(bookRepository, Mockito.times(1)).findById(INVALID_ID_FIVE);
        Mockito.verifyNoMoreInteractions(bookRepository);
    }
}
