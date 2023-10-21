package com.example.onlinebookstore.dto.order;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderRequestDto {
    @NotNull
    private String shippingAddress;
}
