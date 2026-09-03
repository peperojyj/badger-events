package com.badgerevents.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // hibernate가 DB를 조회한 USER Entity 를 만들때 쓰는 빈 생성자
    protected User() {
    }

    private User(
            String email,
            String passwordHash,
            String displayName,
            UserRole role
    ) {
        this.email = Objects.requireNonNull(email);
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.displayName = Objects.requireNonNull(displayName);
        this.role = Objects.requireNonNull(role);

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static User register(
            String email,
            String passwordHash,
            String displayName
    ) {
        return new User(
                email,
                passwordHash,
                displayName,
                UserRole.USER //클라이언트가 스스로 관리자가 되는 것을 요청하지 않게! ROLE 은 매개변수로 받지 않음
        );
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UserRole getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}