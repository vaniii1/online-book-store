package com.example.onlinebookstore.service.cart;

import com.example.onlinebookstore.dto.cart.CartItemQuantityDto;
import com.example.onlinebookstore.dto.cart.CartItemRequestDto;
import com.example.onlinebookstore.dto.cart.CartItemResponseDto;
import com.example.onlinebookstore.dto.cart.CartResponseDto;

public interface ShoppingCartService {
    CartResponseDto getCartForCurrentUser();

    CartResponseDto addBookToCart(CartItemRequestDto request);

    CartItemResponseDto updateQuantityOfBook(Long bookId,
                                             CartItemQuantityDto request);

    void deleteItemFromShoppingCart(Long id);
}
