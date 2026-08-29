package com.ecommerce.project.repositories;

import com.ecommerce.project.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OAuthAccountRepository extends JpaRepository<OAuthAccount, Long> {
    Optional<OAuthAccount> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject);
}
