package com.navi.repository;

import com.navi.domain.AccessTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AccessTokenRepository extends JpaRepository<AccessTokenEntity, Long> {

    Optional<AccessTokenEntity> findFirstBySubjectAndActiveTrueOrderByCreatedAtDesc(String subject);

    List<AccessTokenEntity> findBySubjectAndActiveTrue(String subject);

    List<AccessTokenEntity> findByActiveTrueAndExpiresAtBefore(Instant now);
}