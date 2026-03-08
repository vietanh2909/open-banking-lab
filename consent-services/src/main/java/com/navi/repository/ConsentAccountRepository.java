package com.navi.repository;

import com.navi.domain.ConsentAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ConsentAccountRepository extends JpaRepository<ConsentAccountEntity, Long> {
    List<ConsentAccountEntity> findByConsentId(UUID consentId);
    void deleteByConsentId(UUID consentId);
}