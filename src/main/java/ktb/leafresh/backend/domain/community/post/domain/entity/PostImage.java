package ktb.leafresh.backend.domain.community.post.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "post_images", indexes = @Index(name = "idx_post_image_deleted", columnList = "deleted_at"))
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, length = 512)
    private String imageUrl;

    @Column(nullable = false)
    private Integer sequenceNumber;
}
