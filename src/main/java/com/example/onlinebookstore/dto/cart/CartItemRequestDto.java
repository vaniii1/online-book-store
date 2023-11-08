package com.example.onlinebookstore.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CartItemRequestDto {
    @Min(1)
    @NotNull
    private Long bookId;
    @Min(1)
    @NotNull
    private int quantity;
}
