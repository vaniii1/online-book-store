package com.example.onlinebookstore.dto.cart;

import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CartResponseDto {
    private Long id;
    private Long userId;
    private Set<CartItemResponseDto> items;
}
