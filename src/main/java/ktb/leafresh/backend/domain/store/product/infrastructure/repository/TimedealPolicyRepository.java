package ktb.leafresh.backend.domain.store.product.infrastructure.repository;

import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TimedealPolicyRepository extends JpaRepository<TimedealPolicy, Long> {

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TimedealPolicy t " +
            "WHERE t.product.id = :productId " +
            "AND t.deletedAt IS NULL " +
            "AND (t.startTime < :endTime AND t.endTime > :startTime)")
    boolean existsByProductIdAndTimeOverlap(@Param("productId") Long productId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM TimedealPolicy t " +
            "WHERE t.product.id = :productId " +
            "AND t.deletedAt IS NULL " +
            "AND t.id != :dealId " +
            "AND (t.startTime < :endTime AND t.endTime > :startTime)")
    boolean existsByProductIdAndTimeOverlapExceptSelf(@Param("productId") Long productId,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime,
                                                      @Param("dealId") Long dealId);
}
