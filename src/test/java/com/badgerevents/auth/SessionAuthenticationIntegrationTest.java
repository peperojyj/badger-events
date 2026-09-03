package com.badgerevents.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions
        .assertThat;
import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SessionAuthenticationIntegrationTest {

    private static final String EMAIL =
            "student@example.com";

    private static final String PASSWORD =
            "password123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void registerUser() {
        authService.register(
                new RegisterRequest(
                        EMAIL,
                        PASSWORD,
                        "Bucky Badger"
                )
        );
    }

    @Test
    void csrfEndpointReturnsTheHeaderNameAndToken()
            throws Exception {

        mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.headerName")
                                .value("X-CSRF-TOKEN")
                )
                .andExpect(
                        jsonPath("$.token").isNotEmpty()
                );
    }

    @Test
    void loginStoresAuthenticationInTheSession()
            throws Exception {

        MockHttpSession session = login();

        mockMvc.perform(
                        get("/api/auth/me")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.email")
                                .value(EMAIL)
                )
                .andExpect(
                        jsonPath("$.role")
                                .value("USER")
                );
    }

    @Test
    void loginRejectsAnInvalidPassword()
            throws Exception {

        mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "email": "student@example.com",
                                          "password": "wrong-password"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meRejectsAnUnauthenticatedRequest()
            throws Exception {

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCannotAccessAnAdminEndpoint()
            throws Exception {

        MockHttpSession session = login();

        mockMvc.perform(
                        get("/api/admin/imports")
                                .session(session)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void logoutInvalidatesTheSession()
            throws Exception {

        MockHttpSession session = login();

        mockMvc.perform(
                        post("/api/auth/logout")
                                .session(session)
                                .with(csrf())
                )
                .andExpect(status().isNoContent());

        assertThat(session.isInvalid()).isTrue();
    }

    private MockHttpSession login()
            throws Exception {

        MvcResult result = mockMvc.perform(
                        post("/api/auth/login")
                                .with(csrf())
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "email": "student@example.com",
                                          "password": "password123"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.email")
                                .value(EMAIL)
                )
                .andReturn();

        MockHttpSession session =
                (MockHttpSession) result
                        .getRequest()
                        .getSession(false);

        assertThat(session).isNotNull();

        return session;
    }
}