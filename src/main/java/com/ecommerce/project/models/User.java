package com.ecommerce.project.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 254)
    private String email;
    @Column(name = "display_name", length = 120)
    private String displayName;
    @Column(name = "password_hash", length = 100)
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.CUSTOMER;
    @Column(name = "token_version", nullable = false)
    private long tokenVersion = 0;
    @Column(nullable = false)
    private boolean enabled = true;

    public void incrementTokenVersion() {
        tokenVersion++;
    }
}
