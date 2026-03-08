package com.navi.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "access_tokens",
        indexes = {
                @Index(name = "idx_tokens_subject", columnList = "subject"),
                @Index(name = "idx_tokens_active", columnList = "active"),
                @Index(name = "idx_tokens_expires_at", columnList = "expiresAt")
        })
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
    @Lob
    @Column(nullable = false)
    private String accessToken;

    // thời điểm hết hạn tính theo exp hoặc expires_in
    @Column(nullable = false)
    private Instant expiresAt;

    // tuỳ chọn: refresh token (nếu có)
    @Lob
    private String refreshToken;

    // đánh dấu token còn hiệu lực để bạn disable khi unlink
    @Column(nullable = false)
    private boolean active = true;

    // audit
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant revokedAt;
}