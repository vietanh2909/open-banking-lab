package com.navi.repository;

import com.navi.domain.ConsentEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ConsentEventRepository extends JpaRepository<ConsentEventEntity, UUID> {
    List<ConsentEventEntity> findTop50ByConsentIdOrderByOccurredAtDesc(UUID consentId);
}