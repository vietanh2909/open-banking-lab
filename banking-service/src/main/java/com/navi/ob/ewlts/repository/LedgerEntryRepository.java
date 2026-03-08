package com.navi.ob.ewlts.repository;

import com.navi.ob.ewlts.domain.LedgerEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntryEntity, UUID> {
    boolean existsByPaymentIdAndDirection(String paymentId, String direction);
}