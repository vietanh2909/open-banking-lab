package com.navi.ob.ais.repository;

import com.navi.ob.ais.domain.AisBalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AisBalanceRepo extends JpaRepository<AisBalanceEntity, UUID> {

    // Lock row để tránh double spend khi verify OTP song song
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from AisBalanceEntity b where b.accountId = :accountId")
    Optional<AisBalanceEntity> findByAccountIdForUpdate(@Param("accountId") String accountId);

    Optional<AisBalanceEntity> findByAccountId(String accountId);

    List<AisBalanceEntity> findByAccountIdIn(Collection<String> accountIds);
}