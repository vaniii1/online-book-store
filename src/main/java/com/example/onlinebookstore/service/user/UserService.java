package com.example.onlinebookstore.service.user;

import com.example.onlinebookstore.dto.user.UserRegistrationRequestDto;
import com.example.onlinebookstore.dto.user.UserRegistrationResponseDto;
import com.example.onlinebookstore.exception.RegistrationException;
import com.example.onlinebookstore.model.User;

public interface UserService {
    UserRegistrationResponseDto register(UserRegistrationRequestDto request)
            throws RegistrationException;

    User getCurrentUser();
}
