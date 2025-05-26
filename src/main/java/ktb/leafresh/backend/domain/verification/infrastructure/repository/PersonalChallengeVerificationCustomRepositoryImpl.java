package ktb.leafresh.backend.domain.verification.infrastructure.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PersonalChallengeVerificationCustomRepositoryImpl implements PersonalChallengeVerificationCustomRepository {

    @PersistenceContext
    private final EntityManager em;

    @Override
    public int countConsecutiveSuccessDays(Long memberId) {
        LocalDate thresholdDate = LocalDate.now().minusDays(30);

        List<LocalDate> successDates = em.createQuery("""
        SELECT pcv.createdAt
        FROM PersonalChallengeVerification pcv
        WHERE pcv.member.id = :memberId
          AND pcv.status = 'SUCCESS'
          AND pcv.createdAt >= :threshold
        ORDER BY pcv.createdAt DESC
        """, java.time.LocalDateTime.class)
                .setParameter("memberId", memberId)
                .setParameter("threshold", thresholdDate.atStartOfDay())
                .getResultList().stream()
                .map(java.time.LocalDateTime::toLocalDate)
                .distinct()
                .sorted((d1, d2) -> d2.compareTo(d1))
                .toList();

        // 오늘부터 연속 체크
        int streak = 0;
        LocalDate current = LocalDate.now();

        for (LocalDate date : successDates) {
            if (date.equals(current.minusDays(streak))) {
                streak++;
            } else {
                break;
            }
        }

        return streak;
    }
}
