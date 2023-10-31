package com.example.onlinebookstore.dto.category;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CategoryRequestDto {
    @NotNull
    private String name;
    private String description;
}
