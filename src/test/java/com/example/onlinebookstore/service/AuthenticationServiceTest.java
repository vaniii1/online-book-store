package com.example.onlinebookstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.example.onlinebookstore.dto.user.UserLoginRequestDto;
import com.example.onlinebookstore.dto.user.UserLoginResponseDto;
import com.example.onlinebookstore.security.JwtUtil;
import com.example.onlinebookstore.security.impl.AuthenticationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    @DisplayName("""
            Must return expected token
            """)
    void authenticate_ValidRequest_Ok() {
        UserLoginResponseDto expected = new UserLoginResponseDto("33.gr.42");
        UserLoginRequestDto request = new UserLoginRequestDto()
                .setUsername("test@com")
                .setPassword("1234");
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword());
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword());

        when(authenticationManager.authenticate(authenticationToken)).thenReturn(authentication);
        when(jwtUtil.generateToken(request.getUsername())).thenReturn(expected.token());

        UserLoginResponseDto actual = authenticationService.authenticate(request);

        assertThat(actual).isNotNull();
        assertThat(actual).isEqualTo(expected);

        Mockito.verify(authenticationManager, times(1)).authenticate(authenticationToken);
        Mockito.verify(jwtUtil, times(1)).generateToken(request.getUsername());
        verifyNoMoreInteractions(authenticationManager, jwtUtil);
    }
}
