package com.example.onlinebookstore.repository.order;

import com.example.onlinebookstore.model.Order;
import com.example.onlinebookstore.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> getOrdersByUser(User user, Pageable pageable);

    boolean existsByIdAndUser(Long id, User user);
}
