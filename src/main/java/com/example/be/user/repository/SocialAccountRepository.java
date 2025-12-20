package com.example.be.user.repository;

import com.example.be.user.domain.SocialAccount;
import com.example.be.user.enums.Provider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

  Optional<SocialAccount> findByProviderAndProviderId(Provider provider, String providerId);
}
