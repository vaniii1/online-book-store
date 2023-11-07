package com.example.onlinebookstore.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.onlinebookstore.dto.user.UserLoginRequestDto;
import com.example.onlinebookstore.dto.user.UserLoginResponseDto;
import com.example.onlinebookstore.dto.user.UserRegistrationRequestDto;
import com.example.onlinebookstore.dto.user.UserRegistrationResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @Sql(scripts = "classpath:database/user/add-users.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/user/delete-users.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("""
            Must login User
            """)
    void login_ValidRequest_OK() throws Exception {
        UserLoginRequestDto request = new UserLoginRequestDto()
                .setUsername("user@com")
                .setPassword("1234");
        String jsonRequest = objectMapper.writeValueAsString(request);
        MvcResult result = mockMvc.perform(
                        post("/auth/login")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        UserLoginResponseDto actual =
                objectMapper.readValue(
                        result.getResponse().getContentAsString(), UserLoginResponseDto.class);
        System.out.println(actual);
        assertThat(actual).isNotNull();
        assertThat(actual.token().length()).isGreaterThan(1);
        assertThat(Stream.of(actual.token().split(""))
                .filter(ch -> ch.equals(".")).count()).isEqualTo(2);
    }

    @Test
    @Sql(scripts = {"classpath:database/role/delete-role-user-connection.sql",
            "classpath:database/role/delete-roles.sql",
            "classpath:database/user/delete-users.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @DisplayName("""
            Must register User
            """)
    void register_ValidRequest_OK() throws Exception {
        UserRegistrationResponseDto expected = new UserRegistrationResponseDto()
                .setId(1L)
                .setEmail("email@com")
                .setFirstName("test")
                .setLastName("user");
        UserRegistrationRequestDto request = new UserRegistrationRequestDto()
                .setEmail("email@com")
                .setPassword("1234")
                .setRepeatPassword("1234")
                .setFirstName("test")
                .setLastName("user");
        String jsonRequest = objectMapper.writeValueAsString(request);
        MvcResult result = mockMvc.perform(
                        post("/auth/register")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        UserRegistrationResponseDto actual =
                objectMapper.readValue(result.getResponse().getContentAsString(),
                        UserRegistrationResponseDto.class);
        assertThat(actual).isNotNull();
        EqualsBuilder.reflectionEquals(actual, expected, "id");
    }
}
