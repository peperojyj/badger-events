package com.badgerevents.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerReturnsCreatedUserWithoutPassword()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/register")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "email": "student@example.com",
                                          "password": "password123",
                                          "displayName": "Bucky Badger"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(
                        jsonPath("$.email")
                                .value("student@example.com")
                )
                .andExpect(
                        jsonPath("$.displayName")
                                .value("Bucky Badger")
                )
                .andExpect(
                        jsonPath("$.role").value("USER")
                )
                .andExpect(
                        jsonPath("$.password").doesNotExist()
                )
                .andExpect(
                        jsonPath("$.passwordHash")
                                .doesNotExist()
                );
    }

    @Test
    void registerRejectsInvalidInput()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/register")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "email": "not-an-email",
                                          "password": "short",
                                          "displayName": ""
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerRejectsDuplicateEmail()
            throws Exception {

        String requestBody = """
                {
                  "email": "student@example.com",
                  "password": "password123",
                  "displayName": "Bucky Badger"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/auth/register")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )
                .andExpect(status().isConflict());
    }
}