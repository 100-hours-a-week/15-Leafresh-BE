package ktb.leafresh.backend.domain.verification.presentation.dto.response;

import ktb.leafresh.backend.domain.verification.domain.entity.Comment;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Builder
public record CommentUpdateResponseDto(
        Long id,
        String content,
        LocalDateTime updatedAt,
        String nickname,
        String profileImageUrl,
        Long parentCommentId,
        List<CommentUpdateResponseDto> replies,
        boolean deleted
) {
    public static CommentUpdateResponseDto from(Comment comment, List<CommentUpdateResponseDto> replies) {
        return CommentUpdateResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .updatedAt(comment.getUpdatedAt())
                .nickname(comment.getMember().getNickname())
                .profileImageUrl(comment.getMember().getImageUrl())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .replies(replies)
                .deleted(comment.getDeletedAt() != null)
                .build();
    }
}
