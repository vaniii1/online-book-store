package com.example.onlinebookstore.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.onlinebookstore.model.ShoppingCart;
import com.example.onlinebookstore.model.User;
import com.example.onlinebookstore.repository.cart.ShoppingCartRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ShoppingCartRepositoryTest {
    private static final Long ID_ONE = 1L;
    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Test
    @Sql(scripts = {"classpath:database/user/add-users.sql",
            "classpath:database/cart/add-carts.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:database/cart/delete-carts.sql",
            "classpath:database/user/delete-users.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("""
            Must return valid cart
            """)
    void findByUser_ValidUser_Ok() {
        User user = new User()
                .setId(ID_ONE)
                .setEmail("user@com")
                .setPassword("$2a$10$LprrKOtVTKQh8tzSYOnb8eL8xCHZ.CtSP587Egm2KLTq94pEHwgpi")
                .setFirstName("user")
                .setLastName("fin");
        Optional<ShoppingCart> cart = shoppingCartRepository.findByUser(user);
        assertThat(cart).isNotEmpty();
        assertThat(cart.get().getId()).isEqualTo(1L);
        assertThat(cart.get().getUser()).isEqualTo(user);
    }
}
