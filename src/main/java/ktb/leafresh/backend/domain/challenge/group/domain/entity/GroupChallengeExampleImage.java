package ktb.leafresh.backend.domain.challenge.group.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import lombok.*;

@Entity
@Table(name = "group_challenge_example_images", indexes = {
        @Index(name = "idx_group_challenge_example_images_deleted_at", columnList = "deleted_at")
})
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupChallengeExampleImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_challenge_id", nullable = false)
    private GroupChallenge groupChallenge;

    @Column(nullable = false, length = 512)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExampleImageType type;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer sequenceNumber;
}
