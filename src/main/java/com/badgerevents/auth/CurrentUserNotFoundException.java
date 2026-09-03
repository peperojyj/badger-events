package com.badgerevents.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class CurrentUserNotFoundException extends RuntimeException {

    public CurrentUserNotFoundException() {
        super("The authenticated user no longer exists.");
    }
}