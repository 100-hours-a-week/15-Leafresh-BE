package ktb.leafresh.backend.domain.verification.infrastructure.repository;

import ktb.leafresh.backend.domain.verification.domain.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  @Query(
      "SELECT c.verification.id, COUNT(c) "
          + "FROM Comment c "
          + "WHERE c.deletedAt IS NULL "
          + "GROUP BY c.verification.id")
  List<Object[]> findAllCommentCountByVerificationId();

  List<Comment> findByParentCommentAndDeletedAtIsNull(Comment parentComment);

  @Query(
      "SELECT c FROM Comment c "
          + "JOIN FETCH c.member "
          + "LEFT JOIN FETCH c.parentComment "
          + "WHERE c.verification.id = :verificationId")
  List<Comment> findAllByVerificationIdWithMember(@Param("verificationId") Long verificationId);
}
