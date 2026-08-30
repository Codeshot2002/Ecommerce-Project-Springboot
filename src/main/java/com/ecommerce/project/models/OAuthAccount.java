package com.ecommerce.project.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "oauth_accounts", uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_subject"}))
@Getter
@Setter
@NoArgsConstructor
public class OAuthAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AuthProvider provider;
    @Column(name = "provider_subject", nullable = false, length = 255)
    private String providerSubject;
}
