package ktb.leafresh.backend.domain.challenge.group.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "group_challenge_categories")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupChallengeCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<GroupChallenge> groupChallenges = new ArrayList<>();

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, length = 512)
    private String imageUrl;

    @Column(nullable = false)
    private Integer sequenceNumber;

    @Column(nullable = false)
    private Boolean activated;

    @PrePersist
    public void prePersist() {
        if (activated == null) activated = true;
    }
}
