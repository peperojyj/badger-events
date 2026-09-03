package com.badgerevents.auth;

import com.badgerevents.user.User;
import com.badgerevents.user.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthUserResponse register(RegisterRequest request) {
        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        String normalizedDisplayName =
                request.displayName().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException();
        }

        String passwordHash =
                passwordEncoder.encode(request.password());

        User user = User.register(
                normalizedEmail,
                passwordHash,
                normalizedDisplayName
        );

        try {
            User savedUser =
                    userRepository.saveAndFlush(user);

            return AuthUserResponse.from(savedUser);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException();
        }
    }

    @Transactional(readOnly = true)
    public AuthUserResponse getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .map(AuthUserResponse::from)
                .orElseThrow(CurrentUserNotFoundException::new);
    }
}