package com.example.onlinebookstore.service.cart.impl;

import com.example.onlinebookstore.dto.cart.CartItemQuantityDto;
import com.example.onlinebookstore.dto.cart.CartItemRequestDto;
import com.example.onlinebookstore.dto.cart.CartItemResponseDto;
import com.example.onlinebookstore.dto.cart.CartResponseDto;
import com.example.onlinebookstore.exception.EntityNotFoundException;
import com.example.onlinebookstore.mapper.CartItemMapper;
import com.example.onlinebookstore.mapper.CartMapper;
import com.example.onlinebookstore.model.Book;
import com.example.onlinebookstore.model.CartItem;
import com.example.onlinebookstore.model.ShoppingCart;
import com.example.onlinebookstore.model.User;
import com.example.onlinebookstore.repository.BookRepository;
import com.example.onlinebookstore.repository.cart.ShoppingCartRepository;
import com.example.onlinebookstore.repository.cartitem.CartItemRepository;
import com.example.onlinebookstore.service.cart.ShoppingCartService;
import com.example.onlinebookstore.service.user.UserService;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final BookRepository bookRepository;
    private final CartItemRepository cartItemRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper itemMapper;
    private final UserService userService;

    @Override
    public CartResponseDto getCartForCurrentUser() {
        ShoppingCart cart = getOrCreateShoppingCartForCurrentUser();
        return convertToResponseDto(cart);
    }

    @Override
    public CartResponseDto addBookToCart(CartItemRequestDto request) {
        ShoppingCart cart = getOrCreateShoppingCartForCurrentUser();
        CartItem item = itemMapper.toModel(request);
        item.setBook(findBookById(request.getBookId()));
        cart.addItemToCart(item);
        cartItemRepository.save(item);
        return convertToResponseDto(cart);
    }

    @Override
    public CartItemResponseDto updateQuantityOfBook(
            Long itemId,
            CartItemQuantityDto request
    ) {
        CartItem item = findCartItemById(itemId);
        item.setQuantity(request.getQuantity());
        return itemMapper.toDto(cartItemRepository.save(item));
    }

    @Override
    public void deleteItemFromShoppingCart(Long id) {
        CartItem cartItem = findCartItemById(id);
        User user = userService.getCurrentUser();
        if (!cartItem.getShoppingCart().getUser().equals(user)) {
            throw new AccessDeniedException(
                    "You are not allowed to delete Cart Item with id: " + id
            );
        }
        cartItemRepository.delete(cartItem);
    }

    public ShoppingCart getOrCreateShoppingCartForCurrentUser() {
        User user = userService.getCurrentUser();
        Optional<ShoppingCart> cart =
                shoppingCartRepository.findByUser(user);
        if (cart.isEmpty()) {
            ShoppingCart newCart = new ShoppingCart();
            newCart.setUser(user);
            return shoppingCartRepository.save(newCart);
        }
        return cart.get();
    }

    private Book findBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new EntityNotFoundException("There is no Book with id: " + bookId));
    }

    private CartItem findCartItemById(Long itemId) {
        return cartItemRepository.findById(itemId)
                .orElseThrow(() ->
                        new EntityNotFoundException("There is no item with id: " + itemId));
    }

    private CartResponseDto convertToResponseDto(ShoppingCart cart) {
        CartResponseDto dto = cartMapper.toDto(cart);
        dto.setUserId(userService.getCurrentUser().getId());
        dto.setItems(cart.getCartItems()
                .stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toSet()));
        return dto;
    }
}
