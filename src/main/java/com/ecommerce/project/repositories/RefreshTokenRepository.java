package com.ecommerce.project.repositories;

import com.ecommerce.project.models.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken t set t.revoked = true where t.user = :user and t.revoked = false")
    void revokeActiveByUser(@Param("user") User user);
}
