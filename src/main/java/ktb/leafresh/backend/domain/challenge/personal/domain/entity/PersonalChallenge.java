package ktb.leafresh.backend.domain.challenge.personal.domain.entity;

import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;

import jakarta.persistence.*;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "personal_challenges")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonalChallenge extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Builder.Default
  @OneToMany(mappedBy = "personalChallenge", cascade = CascadeType.ALL)
  private List<PersonalChallengeExampleImage> exampleImages = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "personalChallenge", cascade = CascadeType.ALL)
  private List<PersonalChallengeVerification> verifications = new ArrayList<>();

  @Column(nullable = false, length = 512)
  private String imageUrl;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private Integer leafReward;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private DayOfWeek dayOfWeek;

  @Column(nullable = false)
  private LocalTime verificationStartTime;

  @Column(nullable = false)
  private LocalTime verificationEndTime;

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

  public void addExampleImage(PersonalChallengeExampleImage image) {
    this.exampleImages.add(image);
    if (image.getPersonalChallenge() == null) {
      image.setPersonalChallenge(this);
    }
  }
}
