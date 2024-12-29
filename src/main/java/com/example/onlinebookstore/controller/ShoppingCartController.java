package com.example.onlinebookstore.controller;

import com.example.onlinebookstore.dto.cart.CartItemQuantityDto;
import com.example.onlinebookstore.dto.cart.CartItemRequestDto;
import com.example.onlinebookstore.dto.cart.CartItemResponseDto;
import com.example.onlinebookstore.dto.cart.CartResponseDto;
import com.example.onlinebookstore.service.cart.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Shopping Cart Management",
        description = "Endpoints which indicate a specific action with ShoppingCart")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/cart")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    @GetMapping
    @Operation(summary = "Get Cart for current User",
            description = "Receive a Shopping Cart of currently "
                    + "logged in User with all added Items to it")
    public CartResponseDto getCart() {
        return shoppingCartService.getCartForCurrentUser();
    }

    @PostMapping
    @Operation(summary = "Add an Item(Book) to Cart",
            description = "Add a new book to the Shopping Cart")
    public CartItemResponseDto addBookToCart(@RequestBody @Valid CartItemRequestDto request) {
        return shoppingCartService.addBookToCart(request);
    }

    @PutMapping("/cart-items/{id}")
    @Operation(summary = "Update a quantity of an Item(Book)",
            description = "Update a quantity of a CartItem by it's Id")
    public CartItemResponseDto updateQuantityOfItem(
            @PathVariable Long id,
            @RequestBody @Valid CartItemQuantityDto request
    ) {
        return shoppingCartService.updateQuantityOfBook(id, request);
    }

    @DeleteMapping("/cart-items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an Item(Book) from Shopping Cart",
            description = "Delete a CartItem that is in your Shopping Cart by it's Id")
    public void deleteItemFromShoppingCart(@PathVariable Long id) {
        shoppingCartService.deleteItemFromShoppingCart(id);
    }
}
