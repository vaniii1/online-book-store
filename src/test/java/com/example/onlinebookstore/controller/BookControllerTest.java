package com.example.onlinebookstore.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.onlinebookstore.dto.book.BookDto;
import com.example.onlinebookstore.dto.book.CreateBookRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerTest {
    protected static MockMvc mockMvc;
    private static final Long ID_ONE = 1L;
    private static final Long ID_TWO = 2L;
    private static final Long ID_THREE = 3L;
    private static final Long ID_FOUR = 4L;
    private static final Long ID_FIVE = 5L;
    private static final Long ID_SIX = 6L;
    private static BookDto firstExpected;
    private static BookDto secondExpected;
    private static BookDto thirdExpected;
    private static BookDto fourthExpected;
    private static BookDto fifthExpected;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired WebApplicationContext webApplicationContext,
            @Autowired DataSource dataSource
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        firstExpected = createFirstExpectedBook();
        secondExpected = createSecondExpectedBook();
        thirdExpected = createThirdExpectedBook();
        fourthExpected = createFourthExpectedBook();
        fifthExpected = createFifthExpectedBook();

        deleteBooks(dataSource);
        deleteCategories(dataSource);
        deleteBookCategoryConnection(dataSource);
        addCategories(dataSource);
        addBooks(dataSource);
        addBookCategoryConnection(dataSource);

    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        deleteBookCategoryConnection(dataSource);
        deleteBooks(dataSource);
        deleteCategories(dataSource);
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    @DisplayName("""
            Must return all books stored in database
            """)
    void getAll_BooksFromDatabase_Ok() throws Exception {
        List<BookDto> expected = new ArrayList<>();
        expected.add(firstExpected);
        expected.add(secondExpected);
        expected.add(thirdExpected);
        expected.add(fourthExpected);
        expected.add(fifthExpected);

        MvcResult result = mockMvc.perform(get("/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        BookDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookDto[].class);

        Assertions.assertEquals(5, actual.length);
        Assertions.assertEquals(expected, Arrays.stream(actual).toList());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    @DisplayName("""
            Must create a book with valid request
            """)
    @Sql(scripts = "classpath:database/book/delete-book-with-id-6-connections.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Sql(scripts = "classpath:database/book/delete-book-with-id-6.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createBook_validInput_Ok() throws Exception {
        CreateBookRequestDto request = new CreateBookRequestDto()
                .setAuthor("Ray Bradbury")
                .setTitle("Fahrenheit 451")
                .setIsbn("235153151")
                .setPrice(BigDecimal.valueOf(42.5))
                .setCategoryIds(new HashSet<>(Set.of(ID_ONE)));
        BookDto expected = new BookDto()
                .setId(ID_SIX)
                .setAuthor(request.getAuthor())
                .setTitle(request.getTitle())
                .setIsbn(request.getIsbn())
                .setPrice(request.getPrice())
                .setCategoryIds(new HashSet<>(Set.of(ID_ONE)));

        String jsonRequest = objectMapper.writeValueAsString(request);
        MvcResult result = mockMvc.perform(
                        post("/books")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        BookDto actual =
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        BookDto.class);

        Assertions.assertNotNull(actual);
        EqualsBuilder.reflectionEquals(actual, expected);
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    @DisplayName("""
            Must return secondExpected with id 2
            """)
    void getBookById_validId_Ok() throws Exception {
        MvcResult result = mockMvc.perform(
                    get("/books/2")
                            .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        BookDto actual =
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        BookDto.class);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(secondExpected, actual);
    }

    @Test
    @Sql(scripts = "classpath:database/book/add-book-to-delete.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/book/delete-book-with-id-6.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    @DisplayName("""
            Must delete book with valid id
            """)
    void deleteById_ValidId_Ok() throws Exception {
        mockMvc.perform(
                delete("/books/6")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    @Sql(scripts = "classpath:database/book/add-book-to-update.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/book/delete-book-with-id-6-connections.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Sql(scripts = "classpath:database/book/delete-book-with-id-6.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("""
            Must return updated BookDto with valid request
            """)
    void updateBookById_ValidId_Ok() throws Exception {
        CreateBookRequestDto updateRequest = new CreateBookRequestDto()
                .setAuthor("William Shakespeare")
                .setTitle("romeo and juliet")
                .setIsbn("135613616")
                .setPrice(BigDecimal.valueOf(52.05))
                .setCategoryIds(new HashSet<>(Set.of(ID_THREE)));
        BookDto expected = new BookDto()
                .setId(ID_SIX)
                .setAuthor(updateRequest.getAuthor())
                .setTitle(updateRequest.getTitle())
                .setIsbn(updateRequest.getIsbn())
                .setPrice(updateRequest.getPrice())
                .setCategoryIds(new HashSet<>(Set.of(ID_THREE)));

        String jsonRequest = objectMapper.writeValueAsString(updateRequest);
        MvcResult result = mockMvc.perform(
                        put("/books/6")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        BookDto actual =
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        BookDto.class);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    @DisplayName("""
            Must return all books with from certain price 
            """)
    void searchBooks_ValidLowestPrice_Ok() throws Exception {
        List<BookDto> expected = new ArrayList<>();
        expected.add(firstExpected);
        expected.add(thirdExpected);
        expected.add(fifthExpected);

        MvcResult result = mockMvc.perform(
                get("/books/search?lowestPrice=40")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andReturn();
        BookDto[] actual =
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        BookDto[].class);

        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected.size(), actual.length);
        Assertions.assertEquals(expected, Arrays.stream(actual).toList());
    }

    private static BookDto createFirstExpectedBook() {
        return new BookDto()
                .setId(ID_ONE)
                .setTitle("Red Riding Hood")
                .setAuthor("Brothers Grimm")
                .setIsbn("51351354315")
                .setPrice(BigDecimal.valueOf(44))
                .setCategoryIds(new HashSet<>(Set.of(ID_TWO)));
    }

    private static BookDto createSecondExpectedBook() {
        return new BookDto()
                .setId(ID_TWO)
                .setTitle("Snow White")
                .setAuthor("Brothers Grimm")
                .setIsbn("315613616")
                .setPrice(BigDecimal.valueOf(33))
                .setCategoryIds(new HashSet<>(Set.of(ID_ONE, ID_TWO)));
    }

    private static BookDto createThirdExpectedBook() {
        return new BookDto()
                .setId(ID_THREE)
                .setTitle("Sherlock Holmes")
                .setAuthor("Conan Doyle")
                .setIsbn("262462462624")
                .setPrice(BigDecimal.valueOf(55))
                .setCategoryIds(new HashSet<>(Set.of(ID_ONE)));
    }

    private static BookDto createFourthExpectedBook() {
        return new BookDto()
                .setId(ID_FOUR)
                .setTitle("Pride And Prejudice")
                .setAuthor("Jane Austen")
                .setIsbn("32624624624")
                .setPrice(BigDecimal.valueOf(22))
                .setCategoryIds(new HashSet<>(Set.of(ID_THREE)));
    }

    private static BookDto createFifthExpectedBook() {
        return new BookDto()
                .setId(ID_FIVE)
                .setTitle("Fairy tail")
                .setAuthor("Stephen King")
                .setIsbn("3562624626")
                .setPrice(BigDecimal.valueOf(66))
                .setCategoryIds(new HashSet<>(Set.of(ID_ONE)));
    }

    @SneakyThrows
    public static void addCategories(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/category/add-three-categories.sql")
            );
        }
    }

    @SneakyThrows
    public static void addBooks(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/book/add-five-books.sql")
            );
        }
    }

    @SneakyThrows
    public static void deleteBooks(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/book/delete-books.sql")
            );
        }
    }

    @SneakyThrows
    public static void deleteCategories(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/category/delete-categories.sql")
            );
        }
    }

    @SneakyThrows
    private static void addBookCategoryConnection(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/book/add-categories-to-books.sql")
            );
        }
    }

    @SneakyThrows
    private static void deleteBookCategoryConnection(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/book/delete-book-category-connection.sql")
            );
        }
    }
}
