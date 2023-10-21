package com.example.onlinebookstore.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemQuantityDto {
    @Min(1)
    @NotNull
    private int quantity;
}
