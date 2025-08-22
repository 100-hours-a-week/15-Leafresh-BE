package ktb.leafresh.backend.domain.verification.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.verification.domain.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Schema(description = "댓글 요약 응답 DTO")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
  "id",
  "content",
  "createdAt",
  "updatedAt",
  "nickname",
  "profileImageUrl",
  "parentCommentId",
  "isMine",
  "deleted",
  "replies"
})
public class CommentSummaryResponseDto {

  @Schema(description = "댓글 ID", example = "1")
  private Long id;

  @Schema(description = "댓글 내용", example = "정말 멋진 인증이네요!")
  private String content;

  @Schema(description = "생성일시", example = "2024-12-20T10:30:00Z")
  private OffsetDateTime createdAt;

  @Schema(description = "수정일시", example = "2024-12-20T10:35:00Z")
  private OffsetDateTime updatedAt;

  @Schema(description = "작성자 닉네임", example = "사용자123")
  private String nickname;

  @Schema(description = "작성자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
  private String profileImageUrl;

  @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "10")
  private Long parentCommentId;

  @JsonProperty("isMine")
  @Schema(description = "내가 작성한 댓글 여부", example = "false")
  private boolean isMine;

  @JsonIgnore
  public boolean isMine() {
    return isMine;
  }

  @Schema(description = "삭제된 댓글 여부", example = "false")
  private boolean deleted;

  @Schema(description = "대댓글 목록")
  @Builder.Default
  private List<CommentSummaryResponseDto> replies = new ArrayList<>();

  public static CommentSummaryResponseDto from(
      Comment comment, Long loginMemberId, boolean includeReplies) {
    boolean isDeleted = comment.getDeletedAt() != null;
    boolean isReply = comment.getParentComment() != null;

    return CommentSummaryResponseDto.builder()
        .id(comment.getId())
        .content(isDeleted ? "삭제된 댓글입니다." : comment.getContent())
        .createdAt(comment.getCreatedAt().atOffset(ZoneOffset.UTC))
        .updatedAt(comment.getUpdatedAt().atOffset(ZoneOffset.UTC))
        .nickname(isDeleted ? "(알수없음)" : comment.getMember().getNickname())
        .profileImageUrl(
            isDeleted
                ? "https://storage.googleapis.com/leafresh-images/init/user_icon.png"
                : comment.getMember().getImageUrl())
        .parentCommentId(isReply ? comment.getParentComment().getId() : null)
        .isMine(loginMemberId != null && Objects.equals(comment.getMember().getId(), loginMemberId))
        .deleted(isDeleted)
        .replies((includeReplies && !isReply) ? new ArrayList<>() : null)
        .build();
  }
}
