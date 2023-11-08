package com.example.onlinebookstore.controller;

import static com.example.onlinebookstore.controller.BookControllerTest.addBooks;
import static com.example.onlinebookstore.controller.BookControllerTest.deleteBooks;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.onlinebookstore.dto.order.OrderItemResponseDto;
import com.example.onlinebookstore.dto.order.OrderRequestDto;
import com.example.onlinebookstore.dto.order.OrderResponseDto;
import com.example.onlinebookstore.dto.order.OrderStatusDto;
import com.example.onlinebookstore.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
public class OrderControllerTest {
    protected static MockMvc mockMvc;
    private static final Long ID_ONE = 1L;
    private static final Long ID_TWO = 2L;
    private static final Long ID_THREE = 3L;
    private static final Long ID_FOUR = 4L;
    private static final Long ID_FIVE = 5L;
    private static final Long ID_SIX = 6L;
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
        deleteOrderItems(dataSource);
        deleteOrders(dataSource);
        deleteBooks(dataSource);
        deleteUsers(dataSource);
        addUsers(dataSource);
        addBooks(dataSource);
        addOrders(dataSource);
        addOrderItems(dataSource);
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        deleteOrderItems(dataSource);
        deleteOrders(dataSource);
        deleteBooks(dataSource);
        deleteUsers(dataSource);
    }

    @Test
    @WithMockUser(username = "admin@com", authorities = {"ROLE_ADMIN"})
    @DisplayName("""
            Must return expected Order 
            """)
    void getOrders_ValidRequest_Ok() throws Exception {
        OrderItemResponseDto firstOrderITem = new OrderItemResponseDto()
                .setId(ID_FOUR)
                .setBookId(ID_FOUR)
                .setQuantity(1);
        OrderItemResponseDto secondOrderITem = new OrderItemResponseDto()
                .setId(ID_FIVE)
                .setBookId(ID_FIVE)
                .setQuantity(2);
        OrderResponseDto expectedOrder = new OrderResponseDto()
                .setId(ID_TWO)
                .setStatus(Order.Status.PENDING)
                .setUserId(ID_TWO)
                .setTotal(BigDecimal.valueOf(154))
                .setOrderItems(new HashSet<>(Set.of(firstOrderITem, secondOrderITem)));

        MvcResult result = mockMvc.perform(
                        get("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        OrderResponseDto[] actual =
                objectMapper.readValue(
                        result.getResponse().getContentAsString(), OrderResponseDto[].class);

        assertThat(actual).isNotNull();
        EqualsBuilder.reflectionEquals(
                Arrays.stream(actual).toList().get(0), expectedOrder, "orderDate");
    }

    @Test
    @Sql(scripts = "classpath:database/order/change-status-to-old.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "admin@com", authorities = {"ROLE_ADMIN"})
    @DisplayName("""
            Must update status
            """)
    void updateStatus_ValidRequest_Ok() throws Exception {
        OrderStatusDto request = new OrderStatusDto().setStatus(Order.Status.DELIVERED);
        OrderItemResponseDto firstOrderITem = new OrderItemResponseDto()
                .setId(ID_ONE)
                .setBookId(ID_ONE)
                .setQuantity(2);
        OrderItemResponseDto secondOrderITem = new OrderItemResponseDto()
                .setId(ID_TWO)
                .setBookId(ID_TWO)
                .setQuantity(1);
        OrderItemResponseDto thirdOrderITem = new OrderItemResponseDto()
                .setId(ID_THREE)
                .setBookId(ID_THREE)
                .setQuantity(2);
        OrderResponseDto expectedOrder = new OrderResponseDto()
                .setId(ID_ONE)
                .setStatus(Order.Status.ON_THE_WAY)
                .setUserId(ID_ONE)
                .setStatus(request.getStatus())
                .setTotal(BigDecimal.valueOf(231))
                .setOrderItems(
                        new HashSet<>(Set.of(firstOrderITem, secondOrderITem, thirdOrderITem)));
        String jsonString = objectMapper.writeValueAsString(request);
        MvcResult result = mockMvc.perform(
                        patch("/orders/1")
                                .content(jsonString)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        OrderResponseDto actual =
                objectMapper.readValue(
                        result.getResponse().getContentAsString(), OrderResponseDto.class);

        assertThat(actual).isNotNull();
        EqualsBuilder.reflectionEquals(actual, expectedOrder, "order_date");
    }

    @Test
    @Sql(scripts = {"classpath:database/cart/create-new-cart.sql",
            "classpath:database/cartitem/create-new-cart-item.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/cartitem/delete-cart-item-6.sql",
            "classpath:database/cart/delete-cart-3.sql",
            "classpath:database/orderitem/delete-order-item-6.sql",
            "classpath:database/order/delete-order-3.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(username = "user@com", authorities = {"ROLE_USER"})
    @DisplayName("""
            Must create new Order
            """)
    void createOrder_ValidRequest_Ok() throws Exception {
        OrderRequestDto request = new OrderRequestDto();
        request.setShippingAddress("address 03");
        OrderItemResponseDto orderItem = new OrderItemResponseDto()
                .setId(ID_SIX)
                .setBookId(ID_ONE)
                .setQuantity(2);
        OrderResponseDto expectedOrder = new OrderResponseDto()
                .setStatus(Order.Status.PENDING)
                .setUserId(ID_ONE)
                .setTotal(BigDecimal.valueOf(88))
                .setOrderItems(
                        new HashSet<>(Set.of(orderItem)));
        String jsonString = objectMapper.writeValueAsString(request);
        MvcResult result = mockMvc.perform(
                        post("/orders")
                                .content(jsonString)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        OrderResponseDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), OrderResponseDto.class);

        assertThat(actual).isNotNull();
        EqualsBuilder.reflectionEquals(actual, expectedOrder, "id");
    }

    @Test
    @WithMockUser(username = "user@com", authorities = {"ROLE_USER"})
    @DisplayName("""
            Must return all Items of the exact Order
            """)
    void getAllOrderItems_ValidRequest_Ok() throws Exception {
        OrderItemResponseDto firstOrderITem = new OrderItemResponseDto()
                .setId(ID_ONE)
                .setBookId(ID_ONE)
                .setQuantity(2);
        OrderItemResponseDto secondOrderITem = new OrderItemResponseDto()
                .setId(ID_TWO)
                .setBookId(ID_TWO)
                .setQuantity(1);
        OrderItemResponseDto thirdOrderITem = new OrderItemResponseDto()
                .setId(ID_THREE)
                .setBookId(ID_THREE)
                .setQuantity(2);
        List<OrderItemResponseDto> expected =
                List.of(firstOrderITem, secondOrderITem, thirdOrderITem);

        MvcResult result = mockMvc.perform(
                get("/orders/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andReturn();

        OrderItemResponseDto[] actual =
                objectMapper.readValue(
                        result.getResponse().getContentAsString(), OrderItemResponseDto[].class);
        assertThat(actual).isNotNull();
        assertThat(Arrays.stream(actual).toList()).isEqualTo(expected);
    }

    @Test
    @WithMockUser(username = "admin@com", authorities = {"ROLE_ADMIN"})
    @DisplayName("""
            Must return the exact Item of the exact Order 
            """)
    void getOrderItemByOrderAndItemIds_ValidRequest_Ok() throws Exception {
        OrderItemResponseDto expected = new OrderItemResponseDto()
                .setId(ID_FIVE)
                .setBookId(ID_FIVE)
                .setQuantity(2);

        MvcResult result = mockMvc.perform(
                        get("/orders/2/items/5")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        OrderItemResponseDto actual =
                objectMapper.readValue(
                        result.getResponse().getContentAsString(), OrderItemResponseDto.class);
        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);
    }

    @SneakyThrows
    public static void addUsers(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/user/add-users.sql")
            );
        }
    }

    @SneakyThrows
    public static void deleteUsers(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/user/delete-users.sql")
            );
        }
    }

    @SneakyThrows
    public static void addOrders(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/order/add-orders.sql")
            );
        }
    }

    @SneakyThrows
    public static void deleteOrders(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/order/delete-orders.sql")
            );
        }
    }

    @SneakyThrows
    private static void addOrderItems(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/orderitem/add-order-items.sql")
            );
        }
    }

    @SneakyThrows
    private static void deleteOrderItems(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/orderitem/delete-order-items.sql")
            );
        }
    }
}
