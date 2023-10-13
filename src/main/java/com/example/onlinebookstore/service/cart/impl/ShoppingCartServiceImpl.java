package com.example.onlinebookstore.service.cart.impl;

import com.example.onlinebookstore.dto.cart.CartItemQuantityRequestDto;
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
import com.example.onlinebookstore.repository.book.BookRepository;
import com.example.onlinebookstore.repository.cart.ShoppingCartRepository;
import com.example.onlinebookstore.repository.item.CartItemRepository;
import com.example.onlinebookstore.repository.user.UserRepository;
import com.example.onlinebookstore.service.cart.ShoppingCartService;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper itemMapper;

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
            CartItemQuantityRequestDto request
    ) {
        CartItem item = findCartItemById(itemId);
        item.setQuantity(request.getQuantity());
        return itemMapper.toDto(cartItemRepository.save(item));
    }

    @Override
    public void deleteItemFromShoppingCart(Long id) {
        CartItem cartItem = findCartItemById(id);
        User user = getCurrentUser();
        if (!cartItem.getShoppingCart().getUser().equals(user)) {
            throw new AccessDeniedException(
                    "You are not allowed to delete Cart Item with id: " + id
            );
        }
        cartItemRepository.delete(cartItem);
    }

    private Book findBookById(Long bookId) {
        return bookRepository.findBookById(bookId)
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
        dto.setUserId(getCurrentUser().getId());
        dto.setItems(cart.getCartItems()
                .stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toSet()));
        return dto;
    }

    private ShoppingCart getOrCreateShoppingCartForCurrentUser() {
        User user = getCurrentUser();
        Optional<ShoppingCart> cart =
                shoppingCartRepository.findByUser(user);
        if (cart.isEmpty()) {
            ShoppingCart newCart = new ShoppingCart();
            newCart.setUser(user);
            return shoppingCartRepository.save(newCart);
        }
        return cart.get();
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return userRepository.findByEmail(
                userDetails.getUsername()).orElseThrow(() ->
                new EntityNotFoundException("There is no User with email: "
                        + userDetails.getUsername())
        );
    }
}
