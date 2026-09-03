package com.badgerevents.auth;

import com.badgerevents.user.User;
import com.badgerevents.user.UserRole;

//회원가입 응답 dto
public record AuthUserResponse(
        Long id,
        String email,
        String displayName,
        UserRole role
) {

    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole()
        );
    }
}