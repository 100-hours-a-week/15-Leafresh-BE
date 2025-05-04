package ktb.leafresh.backend.domain.community.comment.domain.entity;

import jakarta.persistence.*;
import ktb.leafresh.backend.domain.community.post.domain.entity.Post;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.common.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "comments", indexes = @Index(name = "idx_comment_deleted", columnList = "deleted_at"))
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @Column(nullable = false)
    private String content;
}
