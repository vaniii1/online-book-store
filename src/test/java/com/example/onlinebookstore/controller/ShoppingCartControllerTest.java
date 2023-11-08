package com.example.onlinebookstore.controller;

import static com.example.onlinebookstore.controller.BookControllerTest.addBooks;
import static com.example.onlinebookstore.controller.BookControllerTest.deleteBooks;
import static com.example.onlinebookstore.controller.OrderControllerTest.addUsers;
import static com.example.onlinebookstore.controller.OrderControllerTest.deleteUsers;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.onlinebookstore.dto.cart.CartItemQuantityDto;
import com.example.onlinebookstore.dto.cart.CartItemRequestDto;
import com.example.onlinebookstore.dto.cart.CartItemResponseDto;
import com.example.onlinebookstore.dto.cart.CartResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ShoppingCartControllerTest {
    protected static MockMvc mockMvc;
    private static final Long ID_TWO = 2L;
    private static final Long ID_THREE = 3L;
    private static final Long ID_FOUR = 4L;
    private static final Long ID_FIVE = 5L;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired WebApplicationContext webApplicationContext,
            @Autowired DataSource dataSource
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        deleteCartItems(dataSource);
        deleteCarts(dataSource);
        deleteBooks(dataSource);
        deleteUsers(dataSource);
        addUsers(dataSource);
        addBooks(dataSource);
        addCarts(dataSource);
        addItems(dataSource);
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        deleteCartItems(dataSource);
        deleteCarts(dataSource);
        deleteBooks(dataSource);
        deleteUsers(dataSource);
    }

    @Test
    @WithMockUser(username = "admin@com", authorities = {"ROLE_ADMIN"})
    @DisplayName("""
            Must return cart
            """)
    void getCart_ValidRequest_Ok() throws Exception {
        CartItemResponseDto firstItem = new CartItemResponseDto()
                .setId(ID_FOUR)
                .setQuantity(1)
                .setBookId(ID_FOUR)
                .setBookTitle("Pride And Prejudice");
        CartItemResponseDto secondItem = new CartItemResponseDto()
                .setId(ID_FIVE)
                .setQuantity(2)
                .setBookId(ID_FIVE)
                .setBookTitle("Fairy tail");
        CartResponseDto expected = new CartResponseDto()
                .setId(ID_TWO)
                .setUserId(ID_TWO)
                .setItems(new HashSet<>(Set.of(firstItem, secondItem)));

        MvcResult result = mockMvc.perform(
                        get("/cart")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        CartResponseDto actual =
                objectMapper.readValue(
                        result.getResponse().getContentAsString(), CartResponseDto.class);
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Sql(scripts = "classpath:database/cartitem/delete-cart-item-6.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "user@com", authorities = {"ROLE_USER"})
    @DisplayName("""
            Must add Book to Cart 
            """)
    void addBookToCart_ValidRequest_Ok() throws Exception {
        CartItemRequestDto request = new CartItemRequestDto()
                .setBookId(ID_FOUR)
                .setQuantity(2);
        CartItemResponseDto expected = new CartItemResponseDto()
                .setBookTitle("Pride And Prejudice")
                .setBookId(ID_FOUR)
                .setQuantity(request.getQuantity());
        String jsonString = objectMapper.writeValueAsString(request);
        MvcResult result = mockMvc.perform(
                post("/cart")
                        .content(jsonString)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andReturn();

        CartItemResponseDto actual =
                objectMapper.readValue(
                        result.getResponse().getContentAsString(), CartItemResponseDto.class);

        assertThat(actual).isNotNull();
        EqualsBuilder.reflectionEquals(actual, expected, "id");
    }

    @Test
    @WithMockUser(username = "user@com", authorities = {"ROLE_USER"})
    @DisplayName("""
            Must update quantity of CartItem 
            """)
    void updateQuantityOfItem_ValidRequest_Ok() throws Exception {
        CartItemQuantityDto request = new CartItemQuantityDto().setQuantity(3);
        String jsonRequest = objectMapper.writeValueAsString(request);
        MvcResult result = mockMvc.perform(
                        put("/cart/cart-items/3")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        CartItemResponseDto expected = new CartItemResponseDto()
                .setId(ID_THREE)
                .setQuantity(3)
                .setBookId(ID_THREE)
                .setBookTitle("Sherlock Holmes");
        CartItemResponseDto actual =
                objectMapper.readValue(
                        result.getResponse().getContentAsString(), CartItemResponseDto.class);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @WithMockUser(username = "user@com", authorities = {"ROLE_USER"})
    @DisplayName("""
            Must delete Item from ShoppingCart
            """)
    void deleteItemFromShoppingCart_ValidRequest_Ok() throws Exception {
        mockMvc.perform(
                delete("/cart/cart-items/1")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isNoContent())
                .andReturn();
    }

    @SneakyThrows
    private static void addCarts(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/cart/add-carts.sql")
            );
        }
    }

    @SneakyThrows
    private static void addItems(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/cartitem/add-cart-items.sql")
            );
        }
    }

    @SneakyThrows
    private static void deleteCartItems(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/cartitem/delete-cart-items.sql")
            );
        }
    }

    @SneakyThrows
    private static void deleteCarts(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/cart/delete-carts.sql")
            );
        }
    }
}
