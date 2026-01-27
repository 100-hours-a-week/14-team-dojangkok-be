package com.dojangkok.backend.repository;

import com.dojangkok.backend.domain.SocialAuth;
import com.dojangkok.backend.domain.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialAuthRepository extends JpaRepository<SocialAuth, Long> {

    Optional<SocialAuth> findByProviderAndProviderId(Provider provider, String providerId);

    void deleteByMemberId(Long memberId);
}

