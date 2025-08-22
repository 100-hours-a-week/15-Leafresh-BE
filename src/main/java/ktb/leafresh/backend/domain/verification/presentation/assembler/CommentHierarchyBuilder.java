package ktb.leafresh.backend.domain.verification.presentation.assembler;

import ktb.leafresh.backend.domain.verification.domain.entity.Comment;
import ktb.leafresh.backend.domain.verification.presentation.dto.response.CommentSummaryResponseDto;

import java.util.*;

public class CommentHierarchyBuilder {

  private CommentHierarchyBuilder() {}

  public static List<CommentSummaryResponseDto> build(List<Comment> comments, Long loginMemberId) {
    if (comments.isEmpty()) return Collections.emptyList();

    Map<Long, CommentSummaryResponseDto> commentMap = new HashMap<>();
    List<CommentSummaryResponseDto> topLevelComments = new ArrayList<>();

    for (Comment comment : comments) {
      commentMap.put(comment.getId(), CommentSummaryResponseDto.from(comment, loginMemberId, true));
    }

    for (Comment comment : comments) {
      Long parentId =
          Optional.ofNullable(comment.getParentComment()).map(Comment::getId).orElse(null);

      CommentSummaryResponseDto current = commentMap.get(comment.getId());

      if (parentId == null) {
        topLevelComments.add(current);
      } else {
        Comment topParent = getTopLevelParent(comment);
        CommentSummaryResponseDto topParentDto = commentMap.get(topParent.getId());
        if (topParentDto != null && topParentDto.getReplies() != null) {
          topParentDto.getReplies().add(current);
        }
      }
    }

    topLevelComments.sort(Comparator.comparing(CommentSummaryResponseDto::getCreatedAt));
    topLevelComments.forEach(
        c -> c.getReplies().sort(Comparator.comparing(CommentSummaryResponseDto::getCreatedAt)));

    return topLevelComments;
  }

  private static Comment getTopLevelParent(Comment comment) {
    Comment current = comment;
    while (current.getParentComment() != null) {
      current = current.getParentComment();
    }
    return current;
  }
}
