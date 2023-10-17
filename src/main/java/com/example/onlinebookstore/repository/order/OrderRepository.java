package com.example.onlinebookstore.repository.order;

import com.example.onlinebookstore.model.Order;
import com.example.onlinebookstore.model.User;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> getOrdersByUser(User user, Pageable pageable);

    List<Order> getOrdersByUser(User user);

    boolean existsByIdAndUser(Long id, User user);
}
