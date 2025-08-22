package ktb.leafresh.backend.domain.verification.presentation.dto.response;

import ktb.leafresh.backend.domain.verification.domain.entity.Comment;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

@Builder
public record CommentResponseDto(
    Long id,
    String content,
    OffsetDateTime createdAt,
    String nickname,
    String profileImageUrl,
    Long parentCommentId,
    List<CommentResponseDto> replies,
    boolean deleted) {
  public static CommentResponseDto from(Comment comment) {
    return CommentResponseDto.builder()
        .id(comment.getId())
        .content(comment.getContent())
        .createdAt(comment.getCreatedAt().atOffset(ZoneOffset.UTC))
        .nickname(comment.getMember().getNickname())
        .profileImageUrl(comment.getMember().getImageUrl())
        .parentCommentId(
            comment.getParentComment() != null ? comment.getParentComment().getId() : null)
        .replies(Collections.emptyList()) // 새 댓글은 자식이 없으므로 빈 리스트
        .deleted(comment.getDeletedAt() != null)
        .build();
  }
}
