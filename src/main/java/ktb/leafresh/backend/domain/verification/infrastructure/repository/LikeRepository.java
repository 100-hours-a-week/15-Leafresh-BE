package ktb.leafresh.backend.domain.verification.infrastructure.repository;

import ktb.leafresh.backend.domain.verification.domain.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface LikeRepository extends JpaRepository<Like, Long> {

    @Query("SELECT l.verification.id, COUNT(l) " +
            "FROM Like l " +
            "WHERE l.deletedAt IS NULL " +
            "GROUP BY l.verification.id")
    List<Object[]> findAllLikeCountByVerificationId();

    @Query("""
    SELECT l.verification.id
    FROM Like l
    WHERE l.member.id = :memberId AND l.verification.id IN :verificationIds
    """)
    Set<Long> findLikedVerificationIdsByMemberId(@Param("memberId") Long memberId,
                                                 @Param("verificationIds") List<Long> verificationIds);


    boolean existsByVerificationIdAndMemberIdAndDeletedAtIsNull(Long verificationId, Long memberId);

    Optional<Like> findByVerificationIdAndMemberIdAndDeletedAtIsNull(Long verificationId, Long memberId);

    Optional<Like> findByVerificationIdAndMemberId(Long verificationId, Long memberId);
}
