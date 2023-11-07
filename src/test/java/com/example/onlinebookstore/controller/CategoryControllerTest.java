package com.example.onlinebookstore.controller;

import static com.example.onlinebookstore.controller.BookControllerTest.addCategories;
import static com.example.onlinebookstore.controller.BookControllerTest.deleteCategories;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.onlinebookstore.dto.book.BookDtoWithoutCategoryIds;
import com.example.onlinebookstore.dto.category.CategoryRequestDto;
import com.example.onlinebookstore.dto.category.CategoryResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CategoryControllerTest {
    protected static MockMvc mockMvc;
    private static final Long ID_ONE = 1L;
    private static final Long ID_TWO = 2L;
    private static final Long ID_THREE = 3L;
    private static final Long ID_FOUR = 4L;
    private static final Long ID_FIVE = 5L;
    private static CategoryResponseDto firstExpected;
    private static CategoryResponseDto secondExpected;
    private static CategoryResponseDto thirdExpected;
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
        firstExpected = createFirstCategoryResponse();
        secondExpected = createSecondCategoryResponse();
        thirdExpected = createThirdCategoryResponse();
        deleteCategories(dataSource);
        addCategories(dataSource);
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        deleteCategories(dataSource);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    @Sql(scripts = "classpath:database/category/delete-category-to-save.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void save_ValidRequest_Ok() throws Exception {
        CategoryRequestDto request = new CategoryRequestDto()
                .setName("category to save");
        CategoryResponseDto expected = new CategoryResponseDto()
                .setName(request.getName());

        String jsonRequest = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/categories")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        CategoryResponseDto actual =
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        CategoryResponseDto.class);

        assertThat(actual).isNotNull();
        EqualsBuilder.reflectionEquals(actual, expected, "id");
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void getById_ValidId_Ok() throws Exception {
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andReturn();

        CategoryResponseDto actual =
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        CategoryResponseDto.class);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(firstExpected);
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void getAll_ValidRequest_Ok() throws Exception {
        List<CategoryResponseDto> expected = List.of(firstExpected, secondExpected, thirdExpected);
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andReturn();
        CategoryResponseDto[] actual =
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        CategoryResponseDto[].class);
        assertThat(actual).isNotNull();
        assertThat(expected).isEqualTo(Arrays.stream(actual).toList());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    @Sql(scripts = "classpath:database/category/add-category-with-id-4.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/category/delete-category-with-id-4.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void update_ValidRequest_Ok() throws Exception {
        CategoryRequestDto request = new CategoryRequestDto()
                .setName("biography");
        CategoryResponseDto expected = new CategoryResponseDto()
                .setId(ID_FOUR)
                .setName(request.getName());
        String jsonRequest = objectMapper.writeValueAsString(request);
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.put("/categories/4")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andReturn();
        CategoryResponseDto actual =
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        CategoryResponseDto.class);
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    @Sql(scripts = "classpath:database/category/add-category-with-id-4.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/category/delete-category-with-id-4.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void deleteById_ValidRequest_Ok() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.delete("/categories/4")
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    @Sql(scripts = "classpath:database/book/add-five-books.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/book/add-categories-to-books.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/book/delete-book-category-connection.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Sql(scripts = "classpath:database/book/delete-books.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAllByCategoryIds_ValidRequest_Ok() throws Exception {
        BookDtoWithoutCategoryIds firstBookExpected =
                new BookDtoWithoutCategoryIds()
                .setId(ID_TWO)
                .setTitle("Snow White")
                .setAuthor("Brothers Grimm")
                .setIsbn("315613616")
                .setPrice(BigDecimal.valueOf(33));
        BookDtoWithoutCategoryIds secondBookExpected =
                new BookDtoWithoutCategoryIds()
                .setId(ID_THREE)
                .setTitle("Sherlock Holmes")
                .setAuthor("Conan Doyle")
                .setIsbn("262462462624")
                .setPrice(BigDecimal.valueOf(55));
        BookDtoWithoutCategoryIds thirdBookExpected =
                new BookDtoWithoutCategoryIds()
                .setId(ID_FIVE)
                .setTitle("Fairy tail")
                .setAuthor("Stephen King")
                .setIsbn("3562624626")
                .setPrice(BigDecimal.valueOf(66));
        List<BookDtoWithoutCategoryIds> expected =
                List.of(firstBookExpected, secondBookExpected, thirdBookExpected);

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/categories/1/books")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andReturn();
        BookDtoWithoutCategoryIds[] actual =
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        BookDtoWithoutCategoryIds[].class);
        assertThat(actual).isNotNull();
        assertThat(Arrays.stream(actual).toList()).isEqualTo(expected);
    }

    private static CategoryResponseDto createFirstCategoryResponse() {
        return new CategoryResponseDto()
                .setId(ID_ONE)
                .setName("fantasy")
                .setDescription("actions cannot be returned in the real world");
    }

    private static CategoryResponseDto createSecondCategoryResponse() {
        return new CategoryResponseDto()
                .setId(ID_TWO)
                .setName("fairy tail")
                .setDescription("short stories for children");
    }

    private static CategoryResponseDto createThirdCategoryResponse() {
        return new CategoryResponseDto()
                .setId(ID_THREE)
                .setName("romance")
                .setDescription("story about lovers");
    }
}
