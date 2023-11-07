package com.example.onlinebookstore.repository;

import static com.example.onlinebookstore.controller.OrderControllerTest.addOrders;
import static com.example.onlinebookstore.controller.OrderControllerTest.addUsers;
import static com.example.onlinebookstore.controller.OrderControllerTest.deleteOrders;
import static com.example.onlinebookstore.controller.OrderControllerTest.deleteUsers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.example.onlinebookstore.model.Order;
import com.example.onlinebookstore.model.User;
import com.example.onlinebookstore.repository.order.OrderRepository;
import java.math.BigDecimal;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest {
    private static final Long ID_ONE = 1L;
    private static final Long INVALID_ID = 12L;
    private static User user;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeAll
    static void beforeAll(@Autowired DataSource dataSource) {
        user = new User()
                .setId(ID_ONE)
                .setEmail("user@com")
                .setPassword("1234")
                .setFirstName("user")
                .setLastName("fin");
        deleteOrders(dataSource);
        deleteUsers(dataSource);
        addUsers(dataSource);
        addOrders(dataSource);
    }

    @AfterAll
    static void afterAll(@Autowired DataSource dataSource) {
        deleteOrders(dataSource);
        deleteUsers(dataSource);
    }

    @Test
    @DisplayName("""
            Must return valid order
            """)
    void getOrdersByUser_ValidUser_Ok() {
        Page<Order> actual = orderRepository.getOrdersByUser(user,
                PageRequest.of(0, 5));
        assertEquals(1, actual.stream().toList().size());
        assertEquals(BigDecimal.valueOf(231), actual.toList().get(0).getTotal());
        assertEquals("address 08", actual.toList().get(0).getShippingAddress());
    }

    @Test
    @DisplayName("""
            Must return false for invalid Id
            """)
    void existsByIdAndUser_ValidUserButNotId_False() {
        boolean actual = orderRepository.existsByIdAndUser(INVALID_ID, user);
        assertFalse(actual);
    }
}
