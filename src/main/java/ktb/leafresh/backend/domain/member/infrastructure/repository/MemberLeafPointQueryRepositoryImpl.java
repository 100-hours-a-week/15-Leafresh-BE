package ktb.leafresh.backend.domain.member.infrastructure.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import ktb.leafresh.backend.domain.member.domain.entity.QMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberLeafPointQueryRepositoryImpl implements MemberLeafPointQueryRepository {

    private final EntityManager em;

    @Override
    public int getTotalLeafPointSum() {
        JPAQueryFactory query = new JPAQueryFactory(em);
        QMember member = QMember.member;

        Integer sum = query
                .select(member.totalLeafPoints.sum())
                .from(member)
                .where(member.deletedAt.isNull())
                .fetchOne();

        return sum != null ? sum : 0;
    }
}
