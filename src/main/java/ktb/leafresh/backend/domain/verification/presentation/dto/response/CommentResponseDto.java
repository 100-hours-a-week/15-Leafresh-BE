package ktb.leafresh.backend.domain.verification.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.verification.domain.entity.Comment;
import lombok.Builder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;

@Schema(description = "댓글 응답 DTO")
@Builder
public record CommentResponseDto(
    @Schema(description = "댓글 ID", example = "1") Long id,
    @Schema(description = "댓글 내용", example = "정말 멋진 인증이네요!") String content,
    @Schema(description = "생성일시", example = "2024-12-20T10:30:00Z") OffsetDateTime createdAt,
    @Schema(description = "작성자 닉네임", example = "사용자123") String nickname,
    @Schema(description = "작성자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImageUrl,
    @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "10") Long parentCommentId,
    @Schema(description = "대댓글 목록") List<CommentResponseDto> replies,
    @Schema(description = "삭제된 댓글 여부", example = "false") boolean deleted) {

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
