package com.example.onlinebookstore.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemQuantityRequestDto {
    @Min(1)
    @NotNull
    private int quantity;
}
