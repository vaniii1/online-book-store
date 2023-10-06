package com.example.onlinebookstore.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserLoginRequestDto {
    @NotBlank
    @Size(min = 6, max = 40)
    private String username;
    @NotBlank
    @Size(min = 4, max = 50)
    private String password;
}
