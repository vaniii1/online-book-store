package com.example.onlinebookstore.service.order.impl;

import com.example.onlinebookstore.dto.order.OrderItemResponseDto;
import com.example.onlinebookstore.dto.order.OrderRequestDto;
import com.example.onlinebookstore.dto.order.OrderResponseDto;
import com.example.onlinebookstore.dto.order.OrderStatusDto;
import com.example.onlinebookstore.exception.EntityNotFoundException;
import com.example.onlinebookstore.mapper.OrderItemMapper;
import com.example.onlinebookstore.mapper.OrderMapper;
import com.example.onlinebookstore.model.Order;
import com.example.onlinebookstore.model.OrderItem;
import com.example.onlinebookstore.model.ShoppingCart;
import com.example.onlinebookstore.model.User;
import com.example.onlinebookstore.repository.cart.ShoppingCartRepository;
import com.example.onlinebookstore.repository.order.OrderRepository;
import com.example.onlinebookstore.repository.orderitem.OrderItemRepository;
import com.example.onlinebookstore.service.order.OrderService;
import com.example.onlinebookstore.service.user.UserService;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserService userService;

    @Override
    public List<OrderResponseDto> getOrdersForCurrentUser(Pageable pageable) {
        return orderRepository.getOrdersByUser(userService.getCurrentUser(), pageable)
                .stream()
                .map(this::convertToDtoOrderAndSetItemsDto)
                .toList();
    }

    @Override
    public OrderResponseDto createOrder(OrderRequestDto request) {
        ShoppingCart cart = getShoppingCartByUser(userService.getCurrentUser());
        Order order = new Order();
        order.setUser(userService.getCurrentUser());
        cart.getCartItems().stream()
                .map(orderItemMapper::convertCartItemToOrderItem)
                .forEach(item -> item.addOrder(order));
        order.setTotal(getTotal(order));
        order.setShippingAddress(request.getShippingAddress());
        orderItemRepository.saveAll(orderRepository.save(order).getOrderItems());
        return convertToDtoOrderAndSetItemsDto(order);
    }

    @Override
    public OrderResponseDto updateOrderStatus(Long id, OrderStatusDto statusDto) {
        Order order = getOrderById(id);
        order.setStatus(statusDto.getStatus());
        return convertToDtoOrderAndSetItemsDto(orderRepository.save(order));
    }

    @Override
    public List<OrderItemResponseDto> getAllOrderItems(Long id, Pageable pageable) {
        accessVerifyForOrderId(id);
        return orderItemRepository.getOrderItemsByOrder(getOrderById(id), pageable)
                .stream()
                .map(orderItemMapper::toDto)
                .toList();
    }

    @Override
    public OrderItemResponseDto getOrderItemByOrderAndItemIds(Long orderId, Long itemId) {
        accessVerifyForOrderId(orderId);
        return getOrderById(orderId).getOrderItems()
                .stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .map(orderItemMapper::toDto)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "There is no item with id: " + itemId
                                        + " for Order with id: " + orderId
                        ));
    }

    private OrderResponseDto convertToDtoOrderAndSetItemsDto(Order order) {
        OrderResponseDto response = orderMapper.toDto(order);
        response.setOrderItems(order.getOrderItems()
                .stream()
                .map(orderItemMapper::toDto)
                .collect(Collectors.toSet())
        );
        return response;
    }

    private BigDecimal multiplyQuantityByPrice(OrderItem item) {
        BigDecimal price = item.getPrice();
        int quantity = item.getQuantity();
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    private BigDecimal getTotal(Order order) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getOrderItems()) {
            total = total.add(multiplyQuantityByPrice(item));
        }
        return total;
    }

    private ShoppingCart getShoppingCartByUser(User user) {
        return shoppingCartRepository.findByUser(user)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "There is no Shopping Cart for User: "
                                        + user
                        ));
    }

    private Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("There is no Order with id: " + id));
    }

    private void accessVerifyForOrderId(Long id) {
        boolean exists = orderRepository.existsByIdAndUser(id, userService.getCurrentUser());
        if (!exists) {
            throw new AccessDeniedException("Your are not allowed to Order with id: " + id);
        }
    }
}
