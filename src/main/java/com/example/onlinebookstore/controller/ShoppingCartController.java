package com.example.onlinebookstore.controller;

import com.example.onlinebookstore.dto.cart.CartItemQuantityRequestDto;
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

@Tag(name = "ShoppingCart management",
        description = "Endpoints which indicate a specific action with ShoppingCart")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/cart")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    @GetMapping
    @Operation(summary = "Get cart for current user",
            description = "Receive a shopping cart of currently "
                    + "logged in user with all added items to it")
    public CartResponseDto getCart() {
        return shoppingCartService.getCartForCurrentUser();
    }

    @PostMapping
    @Operation(summary = "Add an item(book) to cart",
            description = "Add a new book to the shopping cart")
    public CartResponseDto addBookToCart(@RequestBody @Valid CartItemRequestDto request) {
        return shoppingCartService.addBookToCart(request);
    }

    @PutMapping("/cart-items/{id}")
    @Operation(summary = "Update a quantity of an item(book)",
            description = "Update a quantity of a cartItem by it's id")
    public CartItemResponseDto updateQuantityOfItem(
            @PathVariable Long id,
            @RequestBody @Valid CartItemQuantityRequestDto request
    ) {
        return shoppingCartService.updateQuantityOfBook(id, request);
    }

    @DeleteMapping("/cart-items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an item(book) from shopping cart",
            description = "Delete a cartItem that is in your shopping cart by it's id")
    public void deleteItemFromShoppingCart(@PathVariable Long id) {
        shoppingCartService.deleteItemFromShoppingCart(id);
    }
}
