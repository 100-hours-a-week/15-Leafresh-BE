package ktb.leafresh.backend.domain.challenge.group.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import ktb.leafresh.backend.global.util.image.ImageEntity;
import lombok.*;

@Entity
@Table(name = "group_challenge_example_images", indexes = {
        @Index(name = "idx_group_challenge_example_images_deleted_at", columnList = "deleted_at")
})
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupChallengeExampleImage extends BaseEntity implements ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_challenge_id", nullable = false)
    @Setter
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

    public static GroupChallengeExampleImage of(
            GroupChallenge challenge,
            String imageUrl,
            ExampleImageType type,
            String description,
            Integer sequenceNumber
    ) {
        GroupChallengeExampleImage image = GroupChallengeExampleImage.builder()
                .imageUrl(imageUrl)
                .type(type)
                .description(description)
                .sequenceNumber(sequenceNumber)
                .build();

        image.setGroupChallenge(challenge);  // 연관관계만 설정

        return image;
    }

    @Override
    public void updateSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupChallengeExampleImage)) return false;
        GroupChallengeExampleImage other = (GroupChallengeExampleImage) o;
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(id);
    }
}
