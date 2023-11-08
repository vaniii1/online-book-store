package com.example.onlinebookstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.onlinebookstore.dto.order.OrderItemResponseDto;
import com.example.onlinebookstore.dto.order.OrderRequestDto;
import com.example.onlinebookstore.dto.order.OrderResponseDto;
import com.example.onlinebookstore.dto.order.OrderStatusDto;
import com.example.onlinebookstore.exception.EntityNotFoundException;
import com.example.onlinebookstore.mapper.OrderItemMapper;
import com.example.onlinebookstore.mapper.OrderMapper;
import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.model.CartItem;
import com.example.onlinebookstore.model.Order;
import com.example.onlinebookstore.model.OrderItem;
import com.example.onlinebookstore.model.ShoppingCart;
import com.example.onlinebookstore.model.User;
import com.example.onlinebookstore.repository.cart.ShoppingCartRepository;
import com.example.onlinebookstore.repository.order.OrderRepository;
import com.example.onlinebookstore.repository.orderitem.OrderItemRepository;
import com.example.onlinebookstore.service.order.impl.OrderServiceImpl;
import com.example.onlinebookstore.service.user.UserService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    private static final Long ID_ONE = 1L;
    private static final Long ID_TWO = 2L;
    private static final Long INVALID_ID = 10L;
    private static User user;
    private static Order order;
    private static Book firstBook;
    private static Book secondBook;
    private static OrderItem firstOrderItem;
    private static OrderItem secondOrderItem;
    private static OrderResponseDto expectedOrder;
    private static OrderItemResponseDto firstExpectedItem;
    private static OrderItemResponseDto secondExpectedItem;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ShoppingCartRepository cartRepository;
    @Mock
    private OrderItemRepository itemRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private UserService userService;
    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeAll
    static void beforeAll() {
        user = createUser();
        order = new Order()
                .setUser(user)
                .setStatus(Order.Status.PENDING)
                .setShippingAddress("address 08")
                .setTotal(BigDecimal.valueOf(50));
        firstBook = createFirstBook();
        secondBook = createSecondBook();
        firstOrderItem = createFirstItem();
        secondOrderItem = createSecondItem();
        addItemsToOrder();
        firstExpectedItem = createFirstExpectedItem();
        secondExpectedItem = createSecondExpectedItem();
        expectedOrder = createExpectedOrder();
    }

    @Test
    @DisplayName("""
            Must return Order for valid user
            """)
    void getOrdersForCurrentUser_ValidRequest_Ok() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);
        when(userService.getCurrentUser()).thenReturn(user);
        when(orderRepository.getOrdersByUser(user, PageRequest.of(0, 5))).thenReturn(page);
        when(orderMapper.toDto(order)).thenReturn(expectedOrder);
        when(orderItemMapper.toDto(firstOrderItem)).thenReturn(firstExpectedItem);
        when(orderItemMapper.toDto(secondOrderItem)).thenReturn(secondExpectedItem);

        List<OrderResponseDto> actual = orderService.getOrdersForCurrentUser(pageable);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(List.of(expectedOrder));

        verify(userService, times(1)).getCurrentUser();
        verify(orderRepository, times(1)).getOrdersByUser(user, pageable);
        verify(orderMapper, times(1)).toDto(order);
        verify(orderItemMapper, times(1)).toDto(firstOrderItem);
        verify(orderItemMapper, times(1)).toDto(secondOrderItem);
        verifyNoMoreInteractions(userService, orderRepository, orderMapper, orderItemMapper);
    }

    @Test
    @DisplayName("""
            Must create new Order
            """)
    void createOrder_ValidRequest_Ok() {
        OrderRequestDto request = new OrderRequestDto();
        request.setShippingAddress("address 08");
        ShoppingCart cart = new ShoppingCart()
                .setUser(user);
        CartItem firstCartItem = new CartItem()
                .setShoppingCart(cart)
                .setBook(firstBook)
                .setQuantity(1);
        CartItem secondCartItem = new CartItem()
                .setShoppingCart(cart)
                .setBook(secondBook)
                .setQuantity(2);
        cart.getCartItems().add(firstCartItem);
        cart.getCartItems().add(secondCartItem);
        when(userService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(orderItemMapper.convertCartItemToOrderItem(firstCartItem))
                .thenReturn(firstOrderItem);
        when(orderItemMapper.convertCartItemToOrderItem(secondCartItem))
                .thenReturn(secondOrderItem);
        when(orderRepository.save(Mockito.any(Order.class))).thenReturn(order);
        when(itemRepository.saveAll(order.getOrderItems()))
                .thenReturn(order.getOrderItems().stream().toList());
        when(orderMapper.toDto(Mockito.any(Order.class))).thenReturn(expectedOrder);
        when(orderItemMapper.toDto(firstOrderItem)).thenReturn(firstExpectedItem);
        when(orderItemMapper.toDto(secondOrderItem)).thenReturn(secondExpectedItem);

        OrderResponseDto actual = orderService.createOrder(request);
        System.out.println(actual);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expectedOrder);
        verify(userService, times(2)).getCurrentUser();
        verify(cartRepository, times(1)).findByUser(user);
        verify(orderItemMapper, times(1)).convertCartItemToOrderItem(firstCartItem);
        verify(orderItemMapper, times(1)).convertCartItemToOrderItem(secondCartItem);
        verify(orderRepository, times(1)).save(Mockito.any(Order.class));
        verify(itemRepository, times(1)).saveAll(order.getOrderItems());
        verify(orderMapper, times(1)).toDto(Mockito.any(Order.class));
        verify(orderItemMapper, times(1)).toDto(firstOrderItem);
        verify(orderItemMapper, times(1)).toDto(secondOrderItem);
        verifyNoMoreInteractions(userService, cartRepository,
                orderItemMapper, orderRepository, itemRepository, orderMapper);
    }

    @Test
    @DisplayName("""
            Update Status with valid request
            """)
    void updateOrderStatus_ValidRequest_Ok() {
        Order order = new Order()
                .setStatus(Order.Status.PENDING)
                .setOrderItems(new HashSet<>());
        OrderStatusDto request = new OrderStatusDto()
                .setStatus(Order.Status.DELIVERED);
        OrderResponseDto expectedOrder = new OrderResponseDto()
                .setId(ID_TWO)
                .setStatus(Order.Status.DELIVERED)
                .setOrderItems(new HashSet<>());

        when(orderRepository.findById(ID_TWO)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(expectedOrder);

        OrderResponseDto actual = orderService.updateOrderStatus(ID_TWO, request);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expectedOrder);

        verify(orderRepository, times(1)).findById(ID_TWO);
        verify(orderRepository, times(1)).save(order);
        verify(orderMapper, times(1)).toDto(order);
        verifyNoMoreInteractions(orderRepository, orderMapper);
    }

    @Test
    @DisplayName("""
            Return all OrderItems from Order
            """)
    void getAllOrderItems_ValidRequest_Ok() {
        Pageable pageable = PageRequest.of(0, 5);
        List<OrderItem> items = List.of(firstOrderItem, secondOrderItem);
        Page<OrderItem> page = new PageImpl<>(items, pageable, items.size());

        when(userService.getCurrentUser()).thenReturn(user);
        when(orderRepository.existsByIdAndUser(ID_ONE, user)).thenReturn(true);
        when(orderRepository.findById(ID_ONE)).thenReturn(Optional.of(order));
        when(itemRepository.getOrderItemsByOrder(order, pageable)).thenReturn(page);
        when(orderItemMapper.toDto(firstOrderItem)).thenReturn(firstExpectedItem);
        when(orderItemMapper.toDto(secondOrderItem)).thenReturn(secondExpectedItem);

        List<OrderItemResponseDto> actual = orderService.getAllOrderItems(ID_ONE, pageable);
        List<OrderItemResponseDto> expected = List.of(firstExpectedItem, secondExpectedItem);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);

        verify(userService, times(1)).getCurrentUser();
        verify(orderRepository, times(1)).existsByIdAndUser(ID_ONE, user);
        verify(orderRepository, times(1)).findById(ID_ONE);
        verify(itemRepository, times(1)).getOrderItemsByOrder(order, pageable);
        verify(orderItemMapper, times(1)).toDto(firstOrderItem);
        verify(orderItemMapper, times(1)).toDto(secondOrderItem);
        verifyNoMoreInteractions(userService, orderRepository, itemRepository, orderItemMapper);
    }

    @Test
    @DisplayName("""
            Return expected OrderItem
            """)
    void getOrderItemByOrderAndItemIds_ValidIds_Ok() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(orderRepository.existsByIdAndUser(ID_ONE, user)).thenReturn(true);
        when(orderRepository.findById(ID_ONE)).thenReturn(Optional.of(order));
        when(orderItemMapper.toDto(secondOrderItem)).thenReturn(secondExpectedItem);

        OrderItemResponseDto actual = orderService.getOrderItemByOrderAndItemIds(ID_ONE, ID_TWO);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(secondExpectedItem);

        verify(userService, times(1)).getCurrentUser();
        verify(orderRepository, times(1)).existsByIdAndUser(ID_ONE, user);
        verify(orderRepository, times(1)).findById(ID_ONE);
        verify(orderItemMapper, times(1)).toDto(secondOrderItem);
        verifyNoMoreInteractions(userService, orderRepository, orderItemMapper);
    }

    @Test
    @DisplayName("""
            AccessDenied to get Items not from your order
            """)
    void getAllOrderItems_InvalidRequest_ThrowsException() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(orderRepository.existsByIdAndUser(INVALID_ID, user)).thenReturn(false);

        Exception exception = Assertions.assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> orderService.getAllOrderItems(INVALID_ID, PageRequest.of(0, 5))
        );

        String expected = "Your are not allowed to Order with id: " + INVALID_ID;
        String actual = exception.getMessage();

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);

        verify(userService, times(1)).getCurrentUser();
        verify(orderRepository, times(1)).existsByIdAndUser(INVALID_ID, user);
        verifyNoMoreInteractions(userService, orderRepository);
    }

    @Test
    @DisplayName("""
            Throws Exception if ShoppingCart of current User is empty
            """)
    void createOrder_EmptyShippingCart_ThrowsException() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());

        Exception exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> orderService.createOrder(new OrderRequestDto())
        );
        String actual = exception.getMessage();
        String expected = "There is no Shopping Cart for User: " + user;

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);

        verify(userService, times(1)).getCurrentUser();
        verify(cartRepository, times(1)).findByUser(user);
        verifyNoMoreInteractions(userService, cartRepository);
    }

    @Test
    @DisplayName("""
            Throws Exception for invalid OrderId
            """)
    void updateOrderStatus_InvalidId_ThrowsException() {
        when(orderRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        Exception exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> orderService.updateOrderStatus(INVALID_ID, new OrderStatusDto())
        );

        String expected = "There is no Order with id: " + INVALID_ID;
        String actual = exception.getMessage();

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);

        verify(orderRepository, times(1)).findById(INVALID_ID);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    @DisplayName("""
            Throws Exception with invalid ItemId  
            """)
    void getOrderItemByOrderAndItemIds_InvalidItemId_ThrowsException() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(orderRepository.existsByIdAndUser(ID_ONE, user)).thenReturn(true);
        when(orderRepository.findById(ID_ONE)).thenReturn(Optional.of(order));

        Exception exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> orderService
                        .getOrderItemByOrderAndItemIds(ID_ONE, INVALID_ID)
        );

        String expected = "There is no item with id: " + INVALID_ID
                + " for Order with id: " + ID_ONE;
        String actual = exception.getMessage();

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);

        verify(userService, times(1)).getCurrentUser();
        verify(orderRepository, times(1)).existsByIdAndUser(ID_ONE, user);
        verify(orderRepository, times(1)).findById(ID_ONE);
        verifyNoMoreInteractions(userService, orderRepository);
    }

    private static User createUser() {
        return new User()
                .setId(ID_ONE)
                .setEmail("user@com")
                .setFirstName("user")
                .setLastName("fin");
    }

    private static Book createFirstBook() {
        return new Book()
                .setTitle("title 1")
                .setAuthor("sample 1")
                .setPrice(BigDecimal.valueOf(20));
    }

    private static Book createSecondBook() {
        return new Book()
                .setTitle("title 2")
                .setAuthor("sample 2")
                .setPrice(BigDecimal.valueOf(15));
    }

    private static OrderItem createFirstItem() {
        return new OrderItem()
                .setId(ID_ONE)
                .setOrder(order)
                .setBook(firstBook)
                .setQuantity(1)
                .setPrice(BigDecimal.valueOf(20));
    }

    private static OrderItem createSecondItem() {
        return new OrderItem()
                .setId(ID_TWO)
                .setOrder(order)
                .setBook(secondBook)
                .setQuantity(2)
                .setPrice(BigDecimal.valueOf(15));
    }

    private static void addItemsToOrder() {
        order.getOrderItems().add(firstOrderItem);
        order.getOrderItems().add(secondOrderItem);
    }

    private static OrderItemResponseDto createFirstExpectedItem() {
        return new OrderItemResponseDto()
                .setId(ID_ONE)
                .setBookId(ID_ONE)
                .setQuantity(1);
    }

    private static OrderItemResponseDto createSecondExpectedItem() {
        return new OrderItemResponseDto()
                .setId(ID_TWO)
                .setBookId(ID_TWO)
                .setQuantity(2);
    }

    private static OrderResponseDto createExpectedOrder() {
        return new OrderResponseDto()
                .setOrderDate(LocalDateTime.now())
                .setId(ID_ONE)
                .setUserId(ID_ONE)
                .setStatus(Order.Status.PENDING)
                .setTotal(BigDecimal.valueOf(50))
                .setOrderItems(Set.of(firstExpectedItem, secondExpectedItem));
    }
}
