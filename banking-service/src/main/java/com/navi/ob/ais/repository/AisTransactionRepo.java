package com.navi.ob.ais.repository;

import com.navi.ob.ais.domain.AisTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface AisTransactionRepo extends JpaRepository<AisTransactionEntity, UUID> {

    Page<AisTransactionEntity> findByAccountIdAndValueDateBetweenOrderByValueDateDesc(
            String accountId,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable
    );
}
