package com.navi.ob.ais.repository;

import com.navi.ob.ais.domain.AisAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AisAccountRepo extends JpaRepository<AisAccountEntity, UUID> {
    List<AisAccountEntity> findByAccountIdIn(Collection<String> accountIds);

    Optional<AisAccountEntity> findByAccountId(String accountId);
}