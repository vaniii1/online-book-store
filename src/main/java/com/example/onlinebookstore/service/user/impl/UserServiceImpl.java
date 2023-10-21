package com.example.onlinebookstore.service.user.impl;

import com.example.onlinebookstore.dto.user.UserRegistrationRequestDto;
import com.example.onlinebookstore.dto.user.UserRegistrationResponseDto;
import com.example.onlinebookstore.exception.EntityNotFoundException;
import com.example.onlinebookstore.exception.RegistrationException;
import com.example.onlinebookstore.mapper.UserMapper;
import com.example.onlinebookstore.model.Role;
import com.example.onlinebookstore.model.User;
import com.example.onlinebookstore.repository.role.RoleRepository;
import com.example.onlinebookstore.repository.user.UserRepository;
import com.example.onlinebookstore.service.user.UserService;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;

    @Override
    public UserRegistrationResponseDto register(UserRegistrationRequestDto request)
            throws RegistrationException {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RegistrationException(
                    "User with this email: " + request.getEmail() + " already exists");
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        Set<Role> roles = new HashSet<>();
        for (Role role : roleRepository.findAll()) {
            if (role.getName().equals(Role.RoleName.USER)) {
                roles.add(role);
            }
            if (user.getEmail().contains("admin@") && role.getName().equals(Role.RoleName.ADMIN)) {
                roles.add(role);
            }
        }
        user.setRoles(roles);
        return userMapper.toResponseDto(userRepository.save(user));
    }

    @Override
    public User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return userRepository.findByEmail(
                userDetails.getUsername()).orElseThrow(() ->
                new EntityNotFoundException("There is no User with email: "
                        + userDetails.getUsername())
        );
    }
}
