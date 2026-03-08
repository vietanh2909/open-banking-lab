package com.navi.ob.ewlts.repository;

import com.navi.ob.ewlts.domain.EwltsCashInEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EwltsCashInRepository extends JpaRepository<EwltsCashInEntity, String> {
    Optional<EwltsCashInEntity> findByTppIdAndRequestId(String tppId, String requestId);
}