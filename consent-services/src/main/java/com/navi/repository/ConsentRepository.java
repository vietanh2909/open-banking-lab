package com.navi.repository;

import com.navi.domain.ConsentEntity;
import com.navi.domain.ConsentStatus;
import com.navi.domain.ConsentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ConsentRepository extends JpaRepository<ConsentEntity, UUID> {

    Optional<ConsentEntity> findByTppIdAndConsentTypeAndRequestId(String tppId, String consentType, String requestId);

    @Query("""
    select c from ConsentEntity c
    where c.consentType = 'AIS'
      and c.status = 'ACTIVE'
      and c.psuId = :psuId
      and c.tppId = :tppId
      and c.clientId = :clientId
      and (c.validUntil is null or c.validUntil > CURRENT_TIMESTAMP)
    order by c.updatedAt desc
  """)
    Optional<ConsentEntity> findActiveAisConsent(String psuId, String tppId, String clientId);


    Optional<ConsentEntity> findTopByConsentTypeAndStatusAndClientIdAndProviderIdAndPsuIdOrderByUpdatedAtDesc(
            String consentType,
            String status,
            String clientId,
            String providerId,
            String psuId
    );
}
