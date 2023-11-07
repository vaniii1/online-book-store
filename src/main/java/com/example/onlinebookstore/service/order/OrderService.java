package com.example.onlinebookstore.service.order;

import com.example.onlinebookstore.dto.order.OrderItemResponseDto;
import com.example.onlinebookstore.dto.order.OrderRequestDto;
import com.example.onlinebookstore.dto.order.OrderResponseDto;
import com.example.onlinebookstore.dto.order.OrderStatusDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    List<OrderResponseDto> getOrdersForCurrentUser(Pageable pageable);

    OrderResponseDto createOrder(OrderRequestDto request);

    OrderResponseDto updateOrderStatus(Long id, OrderStatusDto status);

    List<OrderItemResponseDto> getAllOrderItems(Long id, Pageable pageable);

    OrderItemResponseDto getOrderItemByOrderAndItemIds(Long orderId, Long itemId);
}
