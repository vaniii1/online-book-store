package com.example.onlinebookstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.onlinebookstore.dto.user.UserRegistrationRequestDto;
import com.example.onlinebookstore.dto.user.UserRegistrationResponseDto;
import com.example.onlinebookstore.exception.RegistrationException;
import com.example.onlinebookstore.mapper.UserMapper;
import com.example.onlinebookstore.model.Role;
import com.example.onlinebookstore.model.User;
import com.example.onlinebookstore.repository.role.RoleRepository;
import com.example.onlinebookstore.repository.user.UserRepository;
import com.example.onlinebookstore.service.user.impl.UserServiceImpl;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private UserMapper mapper;
    @Mock
    private RoleRepository roleRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("""
            Must register new User
            """)
    void register_ValidRequest_Ok() throws RegistrationException {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto()
                .setEmail("testadmin@com")
                .setPassword("1234")
                .setRepeatPassword("1234")
                .setFirstName("john")
                .setLastName("wick");
        Role roleUser = new Role();
        roleUser.setName(Role.RoleName.USER);
        Role roleAdmin = new Role();
        roleAdmin.setName(Role.RoleName.ADMIN);
        List<Role> roles = List.of(roleUser, roleAdmin);
        User user = new User()
                .setRoles(new HashSet<>(roles))
                .setEmail(request.getEmail())
                .setPassword("1234w")
                .setFirstName(request.getFirstName())
                .setLastName(request.getLastName());
        UserRegistrationResponseDto expected = new UserRegistrationResponseDto()
                .setEmail(user.getEmail())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(encoder.encode(request.getPassword())).thenReturn("1234w");
        when(roleRepository.findAll()).thenReturn(roles);
        when(userRepository.save(user)).thenReturn(user);
        when(mapper.toResponseDto(user)).thenReturn(expected);

        UserRegistrationResponseDto actual = userService.register(request);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);

        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verify(encoder, times(1)).encode(request.getPassword());
        verify(roleRepository, times(1)).findAll();
        verify(userRepository, times(1)).save(user);
        verify(mapper, times(1)).toResponseDto(user);
        verifyNoMoreInteractions(userRepository, encoder, roleRepository, mapper);
    }

    @Test
    @DisplayName("""
            Must throw Exception if the User is already registered
            """)
    void register_registeredUser_ThrowsException() {
        UserRegistrationRequestDto request = new UserRegistrationRequestDto()
                .setEmail("exception@com");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        Exception exception = assertThrows(
                RegistrationException.class,
                () -> userService.register(request)
        );

        String expected = "User with this email: " + request.getEmail() + " already exists";
        String actual = exception.getMessage();

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);

        verify(userRepository, times(1)).findByEmail(request.getEmail());
        verifyNoMoreInteractions(userRepository);
    }
}
