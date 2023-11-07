package com.example.onlinebookstore.repository;

import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.model.Order;
import com.example.onlinebookstore.model.OrderItem;
import com.example.onlinebookstore.model.User;
import com.example.onlinebookstore.repository.orderitem.OrderItemRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderItemRepositoryTest {
    private static final Long ID_TWO = 2L;
    private static final Long ID_FOUR = 4L;
    private static final Long ID_FIVE = 5L;
    @Autowired
    private OrderItemRepository itemRepository;

    @Test
    @Sql(scripts = {"classpath:database/user/add-users.sql",
            "classpath:database/book/add-five-books.sql",
            "classpath:database/order/add-orders.sql",
            "classpath:database/orderitem/add-order-items.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/orderitem/delete-order-items.sql",
            "classpath:database/order/delete-orders.sql",
            "classpath:database/user/delete-users.sql",
            "classpath:database/book/delete-books.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getOrderItemsByOrder() {
        User user = new User()
                .setId(ID_TWO)
                .setEmail("admin@com")
                .setFirstName("admin")
                .setLastName("sin")
                .setPassword("1234");
        Order order = new Order()
                .setId(ID_TWO)
                .setUser(user)
                .setTotal(BigDecimal.valueOf(154))
                .setStatus(Order.Status.PENDING);
        Book firstBook = new Book()
                .setId(ID_FOUR)
                .setTitle("Pride And Prejudice")
                .setAuthor("Jane Austen")
                .setIsbn("32624624624")
                .setPrice(BigDecimal.valueOf(22));
        Book secondBook = new Book()
                .setId(ID_FIVE)
                .setTitle("Fairy tail")
                .setAuthor("Stephen King")
                .setIsbn("3562624626")
                .setPrice(BigDecimal.valueOf(66));
        OrderItem firstItemExpected = new OrderItem()
                .setOrder(order)
                .setId(ID_FOUR)
                .setBook(firstBook)
                .setPrice(firstBook.getPrice())
                .setQuantity(1);
        OrderItem secondItemExpected = new OrderItem()
                .setOrder(order)
                .setId(ID_FIVE)
                .setBook(secondBook)
                .setPrice(secondBook.getPrice())
                .setQuantity(2);
        List<OrderItem> expected = List.of(firstItemExpected, secondItemExpected);
        List<OrderItem> actual =
                itemRepository.getOrderItemsByOrder(
                        order, PageRequest.of(0, 5)).toList();

        Assertions.assertEquals(actual.size(), expected.size());
        Assertions.assertEquals(actual.get(0).getBook().getTitle(),
                expected.get(0).getBook().getTitle());
        Assertions.assertEquals(actual.get(1).getBook().getTitle(),
                expected.get(1).getBook().getTitle());
    }
}
