package ktb.leafresh.backend.domain.verification.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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

  private Long id;
  private String content;
  private OffsetDateTime createdAt;
  private OffsetDateTime updatedAt;
  private String nickname;
  private String profileImageUrl;
  private Long parentCommentId;

  @JsonProperty("isMine")
  private boolean isMine;

  @JsonIgnore
  public boolean isMine() {
    return isMine;
  }

  private boolean deleted;

  @Builder.Default private List<CommentSummaryResponseDto> replies = new ArrayList<>();

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
