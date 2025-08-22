package ktb.leafresh.backend.domain.member.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.domain.member.domain.entity.enums.BadgeType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "badges")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Badge {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToMany(mappedBy = "badge", cascade = CascadeType.ALL)
  private List<MemberBadge> memberBadges = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private BadgeType type;

  @Column(nullable = false, length = 30)
  private String name;

  @Column(name = "acquisition_condition", nullable = false, columnDefinition = "TEXT")
  private String condition;

  @Column(nullable = false, length = 512)
  private String imageUrl;
}
