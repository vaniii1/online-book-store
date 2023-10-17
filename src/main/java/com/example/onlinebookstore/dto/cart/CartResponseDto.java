package com.example.onlinebookstore.dto.cart;

import java.util.Set;
import lombok.Data;

@Data
public class CartResponseDto {
    private Long id;
    private Long userId;
    private Set<CartItemResponseDto> items;
}
