package ktb.leafresh.backend.domain.community.post.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.domain.community.post.domain.entity.enums.HashtagStatus;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hashtags")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "hashtag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostHashtag> postHashtags = new ArrayList<>();

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HashtagStatus status;
}
