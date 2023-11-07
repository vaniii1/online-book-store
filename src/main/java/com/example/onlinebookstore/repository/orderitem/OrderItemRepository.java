package com.example.onlinebookstore.repository.orderitem;

import com.example.onlinebookstore.model.Order;
import com.example.onlinebookstore.model.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    Page<OrderItem> getOrderItemsByOrder(Order order, Pageable pageable);
}
