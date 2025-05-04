package ktb.leafresh.backend.global.infrastructure.s3.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "s3_delete_failures")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class S3DeleteFailure extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 512)
    private String imageUrl;

    @Column(nullable = false)
    private String reason;

    private Integer retryCount;

    @Column(nullable = false)
    private LocalDateTime lastFailedAt;

    @Column(nullable = false)
    private LocalDateTime scheduledRetryAt;
}
