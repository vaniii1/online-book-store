package com.example.onlinebookstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.onlinebookstore.dto.cart.CartItemQuantityDto;
import com.example.onlinebookstore.dto.cart.CartItemRequestDto;
import com.example.onlinebookstore.dto.cart.CartItemResponseDto;
import com.example.onlinebookstore.dto.cart.CartResponseDto;
import com.example.onlinebookstore.mapper.CartItemMapper;
import com.example.onlinebookstore.mapper.CartMapper;
import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.model.CartItem;
import com.example.onlinebookstore.model.ShoppingCart;
import com.example.onlinebookstore.model.User;
import com.example.onlinebookstore.repository.book.BookRepository;
import com.example.onlinebookstore.repository.cart.ShoppingCartRepository;
import com.example.onlinebookstore.repository.cartitem.CartItemRepository;
import com.example.onlinebookstore.service.cart.impl.ShoppingCartServiceImpl;
import com.example.onlinebookstore.service.user.UserService;
import java.math.BigDecimal;
import java.util.HashSet;
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

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceTest {
    private static final Long ID_ONE = 1L;
    private static final Long ID_TWO = 2L;
    private static final Long ID_THREE = 3L;
    private static final Long INVALID_ID = 5L;
    private static User user;
    private static ShoppingCart shoppingCart;
    private static CartItem firstItem;
    private static CartItem secondItem;
    private static Book firstBook;
    private static Book secondBook;
    private static CartItemResponseDto firstItemExpected;
    private static CartItemResponseDto secondItemExpected;
    private static CartResponseDto cartExpected;
    @Mock
    private CartItemMapper itemMapper;
    @Mock
    private CartMapper cartMapper;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ShoppingCartRepository cartRepository;
    @Mock
    private UserService userService;
    @InjectMocks
    private ShoppingCartServiceImpl cartService;

    @BeforeAll
    static void beforeAll() {
        user = createUser();
        shoppingCart = createShoppingCart();
        firstBook = createFirstBook();
        secondBook = createSecondBook();
        firstItem = createFirstItem();
        secondItem = createSecondItem();
        addItemsToCart();
        firstItemExpected = createFirstItemExpected();
        secondItemExpected = createSecondItemExpected();
        cartExpected = createCartExpected();
    }

    @Test
    @DisplayName("""
            Must return expected Cart
            """)
    void getCartForCurrentUser_ValidRequest_Ok() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(shoppingCart));
        when(cartMapper.toDto(shoppingCart)).thenReturn(cartExpected);
        when(itemMapper.toDto(firstItem)).thenReturn(firstItemExpected);
        when(itemMapper.toDto(secondItem)).thenReturn(secondItemExpected);

        CartResponseDto actual = cartService.getCartForCurrentUser();

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(cartExpected);

        verify(userService, Mockito.times(2)).getCurrentUser();
        verify(cartRepository, Mockito.times(1)).findByUser(user);
        verify(cartMapper, Mockito.times(1)).toDto(shoppingCart);
        verify(itemMapper, Mockito.times(1)).toDto(firstItem);
        verify(itemMapper, Mockito.times(1)).toDto(secondItem);
        Mockito.verifyNoMoreInteractions(userService, cartRepository, cartMapper, itemMapper);
    }

    @Test
    @DisplayName("""
            Must add CartItem to ShoppingCart
            """)
    void addBookToCart_ValidRequest_Ok() {
        Book book = new Book()
                .setId(ID_THREE)
                .setAuthor("sample author 3")
                .setTitle("sample title 3")
                .setIsbn("313153151353")
                .setPrice(BigDecimal.valueOf(53));
        CartItemRequestDto request = new CartItemRequestDto()
                .setBookId(ID_THREE)
                .setQuantity(1);
        CartItem item = new CartItem()
                .setBook(book)
                .setQuantity(request.getQuantity());
        ShoppingCart cart = new ShoppingCart()
                .setUser(user);
        CartItemResponseDto expectedItem = new CartItemResponseDto()
                .setBookId(book.getId())
                .setBookTitle(book.getTitle())
                .setQuantity(item.getQuantity())
                .setId(ID_THREE);
        when(userService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());
        when(cartRepository.save(Mockito.any(ShoppingCart.class))).thenReturn(cart);
        when((itemMapper.toModel(request))).thenReturn(item);
        when(bookRepository.findById(ID_THREE)).thenReturn(Optional.of(book));
        when(cartItemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(expectedItem);

        CartItemResponseDto actual = cartService.addBookToCart(request);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expectedItem);

        verify(userService, times(1)).getCurrentUser();
        verify(cartRepository, times(1)).findByUser(user);
        verify(cartRepository, times(1)).save(Mockito.any(ShoppingCart.class));
        verify(itemMapper, times(1)).toModel(request);
        verify(bookRepository, times(1)).findById(ID_THREE);
        verify(cartItemRepository, times(1)).save(item);
        verify(itemMapper, times(1)).toDto(item);
        verifyNoMoreInteractions(userService, cartRepository,
                itemMapper, bookRepository, cartItemRepository, cartMapper);
    }

    @Test
    @DisplayName("""
            Must update quantity of CartItem
            """)
    void updateQuantityOfBook_ValidRequest_Ok() {
        CartItemQuantityDto updateRequest = new CartItemQuantityDto()
                .setQuantity(3);
        CartItem item = new CartItem()
                .setQuantity(1);
        CartItem updatedItem = new CartItem()
                .setQuantity(updateRequest.getQuantity());
        CartItemResponseDto expected = new CartItemResponseDto()
                .setQuantity(updatedItem.getQuantity());
        when(cartItemRepository.findById(ID_ONE)).thenReturn(Optional.of(item));
        when(cartItemRepository.save(updatedItem)).thenReturn(updatedItem);
        when(itemMapper.toDto(updatedItem)).thenReturn(expected);

        CartItemResponseDto actual = cartService.updateQuantityOfBook(ID_ONE, updateRequest);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);

        verify(cartItemRepository, Mockito.times(1)).findById(ID_ONE);
        verify(cartItemRepository, Mockito.times(1)).save(updatedItem);
        verify(itemMapper, times(1)).toDto(updatedItem);
        verifyNoMoreInteractions(cartItemRepository, itemMapper);
    }

    @Test
    @DisplayName("""
            Must delete CartItem from ShoppingCart
            """)
    void deleteItemFromShoppingCart_ValidId_Ok() {

        when(userService.getCurrentUser()).thenReturn(user);
        when(cartItemRepository.findById(ID_TWO)).thenReturn(Optional.of(secondItem));

        cartService.deleteItemFromShoppingCart(ID_TWO);

        verify(cartItemRepository, times(1)).delete(secondItem);
        verify(userService, times(1)).getCurrentUser();
        verifyNoMoreInteractions(cartItemRepository, userService);
    }

    @Test
    @DisplayName("""
            Must return EntityNotFoundException with invalid Id
            """)
    void deleteBook_InvalidId_ThrowsException() {
        when(cartItemRepository.findById(INVALID_ID)).thenReturn(Optional.empty());

        Exception exception = assertThrows(
                com.example.onlinebookstore.exception.EntityNotFoundException.class,
                () -> cartService.deleteItemFromShoppingCart(INVALID_ID)
        );

        String expected = "There is no Item with id: " + INVALID_ID;
        String actual = exception.getMessage();

        Assertions.assertEquals(expected, actual);
        verify(cartItemRepository, times(1)).findById(INVALID_ID);
        verifyNoMoreInteractions(cartItemRepository);
    }

    @Test
    @DisplayName("""
            Must return EntityNotFoundException with invalid Id
            """)
    void addBookToCart_InvalidRequest_ThrowsException() {
        CartItemRequestDto request = new CartItemRequestDto()
                .setBookId(INVALID_ID)
                .setQuantity(2);
        CartItem item = new CartItem()
                .setQuantity(request.getQuantity());
        when(userService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(shoppingCart));
        when(itemMapper.toModel(request)).thenReturn(item);

        Exception exception = Assertions.assertThrows(
                com.example.onlinebookstore.exception.EntityNotFoundException.class,
                () -> cartService.addBookToCart(request)
        );

        String expected = "There is no Book with id: " + INVALID_ID;
        String actual = exception.getMessage();

        assertEquals(expected, actual);
        verify(userService, times(1)).getCurrentUser();
        verify(cartRepository, times(1)).findByUser(user);
        verify(itemMapper, times(1)).toModel(request);
        verifyNoMoreInteractions(userService, cartRepository, itemMapper);
    }

    @Test
    @DisplayName("""
            Check if user can delete an item not from his ShoppingCart
            """)
    void deleteItemFromShoppingCart_IncorrectCartIdForUser_ThrowsException() {
        User currentUser = new User();

        when(cartItemRepository.findById(ID_TWO)).thenReturn(Optional.of(secondItem));
        when(userService.getCurrentUser()).thenReturn(currentUser);

        Exception exception = Assertions.assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> cartService.deleteItemFromShoppingCart(ID_TWO)
        );

        String expected = "You are not allowed to delete Cart Item with id: " + ID_TWO;
        String actual = exception.getMessage();

        Assertions.assertEquals(expected, actual);
        verify(cartItemRepository, times(1)).findById(ID_TWO);
        verify(userService, times(1)).getCurrentUser();
        verifyNoMoreInteractions(cartItemRepository, userService);
    }

    private static User createUser() {
        return new User()
                .setId(ID_ONE)
                .setEmail("user@com")
                .setPassword("1234")
                .setFirstName("name")
                .setLastName("lastname");
    }

    private static ShoppingCart createShoppingCart() {
        return new ShoppingCart()
                .setUser(user);
    }

    private static Book createFirstBook() {
        return new Book()
                .setId(ID_ONE)
                .setAuthor("author sample 1")
                .setTitle("title sample 1")
                .setIsbn("3513614146")
                .setPrice(BigDecimal.valueOf(14));
    }

    private static Book createSecondBook() {
        return new Book()
                .setId(ID_TWO)
                .setAuthor("author sample 2")
                .setTitle("title sample 2")
                .setIsbn("35151361651")
                .setPrice(BigDecimal.valueOf(13));
    }

    private static CartItem createFirstItem() {
        return new CartItem()
                .setBook(firstBook)
                .setShoppingCart(shoppingCart)
                .setQuantity(1);
    }

    private static CartItem createSecondItem() {
        return new CartItem()
                .setBook(secondBook)
                .setShoppingCart(shoppingCart)
                .setQuantity(2);
    }

    private static void addItemsToCart() {
        shoppingCart.getCartItems().add(firstItem);
        shoppingCart.getCartItems().add(secondItem);
    }

    private static CartItemResponseDto createFirstItemExpected() {
        return new CartItemResponseDto()
                .setId(ID_ONE)
                .setBookId(ID_ONE)
                .setBookTitle(firstItem.getBook().getTitle())
                .setQuantity(firstItem.getQuantity());
    }

    private static CartItemResponseDto createSecondItemExpected() {
        return new CartItemResponseDto()
                .setId(ID_TWO)
                .setBookId(ID_TWO)
                .setBookTitle(secondItem.getBook().getTitle())
                .setQuantity(secondItem.getQuantity());
    }

    private static CartResponseDto createCartExpected() {
        return new CartResponseDto()
                .setId(ID_ONE)
                .setUserId(ID_ONE)
                .setItems(new HashSet<>(Set.of(firstItemExpected, secondItemExpected)));
    }
}
