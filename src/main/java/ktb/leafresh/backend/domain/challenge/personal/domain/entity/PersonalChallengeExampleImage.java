package ktb.leafresh.backend.domain.challenge.personal.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import lombok.*;

@Entity
@Table(
    name = "personal_challenge_example_images",
    indexes = {
      @Index(name = "idx_personal_challenge_example_images_deleted_at", columnList = "deleted_at")
    })
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalChallengeExampleImage extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "personal_challenge_id", nullable = false)
  @Setter
  private PersonalChallenge personalChallenge;

  @Column(nullable = false, length = 512)
  private String imageUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ExampleImageType type;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private Integer sequenceNumber;

  public static PersonalChallengeExampleImage of(
      PersonalChallenge challenge,
      String imageUrl,
      ExampleImageType type,
      String description,
      int sequenceNumber) {
    PersonalChallengeExampleImage image =
        PersonalChallengeExampleImage.builder()
            .imageUrl(imageUrl)
            .type(type)
            .description(description)
            .sequenceNumber(sequenceNumber)
            .build();

    image.setPersonalChallenge(challenge);
    return image;
  }
}
