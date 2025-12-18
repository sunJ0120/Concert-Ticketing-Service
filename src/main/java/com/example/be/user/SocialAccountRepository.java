package com.example.be.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

  Optional<SocialAccount> findByProviderAndProviderId(Provider provider, String providerId);
}
