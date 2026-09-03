package com.badgerevents.auth;

import org.springframework.security.web.csrf.CsrfToken;

// CSRF 응답 DTO
public record CsrfTokenResponse(
        String headerName,
        String parameterName,
        String token
) {

    public static CsrfTokenResponse from(CsrfToken csrfToken) {
        return new CsrfTokenResponse(
                csrfToken.getHeaderName(),
                csrfToken.getParameterName(),
                csrfToken.getToken()
        );
    }
}