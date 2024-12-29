package com.example.onlinebookstore.controller;

import com.example.onlinebookstore.dto.order.OrderItemResponseDto;
import com.example.onlinebookstore.dto.order.OrderRequestDto;
import com.example.onlinebookstore.dto.order.OrderResponseDto;
import com.example.onlinebookstore.dto.order.OrderStatusDto;
import com.example.onlinebookstore.service.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order Management",
        description = "Endpoints which indicate a specific action with Order")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/orders")
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Get Orders for current User",
            description = "Receive list of Orders of currently "
                    + "logged in User with all added Items to it")
    public List<OrderResponseDto> getOrders(Pageable pageable) {
        return orderService.getOrdersForCurrentUser(pageable);
    }

    @PostMapping
    @Operation(summary = "Create a new Order",
            description = "Create a new Order based on Shopping Cart "
                    + "with Shipping Address in request")
    public OrderResponseDto createOrder(@RequestBody OrderRequestDto request) {
        return orderService.createOrder(request);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{id}")
    @Operation(summary = "Update a status of an Order",
            description = "Update a status of an Order by it's Id")
    public OrderResponseDto updateStatus(@PathVariable Long id,
                             @Valid @RequestBody OrderStatusDto status) {
        return orderService.updateOrderStatus(id, status);
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "Receive Items from Order",
            description = "Receive all Items from Order by orderId")
    public List<OrderItemResponseDto> getAllOrderItems(@PathVariable Long id, Pageable pageable) {
        return orderService.getAllOrderItems(id, pageable);
    }

    @GetMapping("/{orderId}/items/{itemId}")
    @Operation(summary = "Receive an Item from Order",
            description = "Receive an Item from Order based on Order and Item Ids'")
    public OrderItemResponseDto getOrderItemByOrderAndItemIds(
            @PathVariable Long orderId,
            @PathVariable Long itemId
    ) {
        return orderService.getOrderItemByOrderAndItemIds(orderId, itemId);
    }
}

