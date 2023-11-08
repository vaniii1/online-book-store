package com.example.onlinebookstore.dto.order;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class OrderItemResponseDto {
    private Long id;
    private Long bookId;
    private int quantity;
}
