package ktb.leafresh.backend.domain.challenge.personal.domain.entity;

import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;

import jakarta.persistence.*;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
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

    @OneToMany(mappedBy = "personalChallenge", cascade = CascadeType.ALL)
    private List<PersonalChallengeExampleImage> exampleImages = new ArrayList<>();

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

    @Column(nullable = false, length = 20)
    private String dayOfWeek;

    @Column(nullable = false)
    private LocalTime verificationStartTime;

    @Column(nullable = false)
    private LocalTime verificationEndTime;
}
