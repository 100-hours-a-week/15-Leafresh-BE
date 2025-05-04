package ktb.leafresh.backend.domain.member.domain.entity;

import jakarta.persistence.*;
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

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, length = 100)
    private String description;

    @Column(nullable = false, length = 512)
    private String imageUrl;
}
