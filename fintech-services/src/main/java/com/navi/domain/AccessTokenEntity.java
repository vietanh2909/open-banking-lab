package com.navi.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "access_tokens",
        indexes = {
                @Index(name = "idx_tokens_subject", columnList = "subject"),
                @Index(name = "idx_tokens_active", columnList = "active"),
                @Index(name = "idx_tokens_expires_at", columnList = "expiresAt")
        })
@Getter
@Setter
public class AccessTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ai đang sở hữu token (psu_id / user_id / sub trong token)
    @Column(nullable = false, length = 128)
    private String subject;

    // ví dụ: "ais" / "pis" hoặc scopes string
    @Column(nullable = false, length = 512)
    private String scope;

    // access token (khuyến nghị mã hoá trước khi lưu)
    @Column(nullable = false, columnDefinition = "text")
    private String accessToken;

    // thời điểm hết hạn tính theo exp hoặc expires_in
    @Column(nullable = false)
    private Instant expiresAt;

    // tuỳ chọn: refresh token (nếu có)
    @Column(nullable = false, columnDefinition = "text")
    private String refreshToken;

    // đánh dấu token còn hiệu lực để bạn disable khi unlink
    @Column(nullable = false)
    private boolean active = true;

    // audit
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant revokedAt;

    @Column(name = "consent_id", length = 50)
    private String consentId;
}