package ktb.leafresh.backend.domain.store.product.infrastructure.repository;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ktb.leafresh.backend.domain.store.product.domain.entity.QProduct;
import ktb.leafresh.backend.domain.store.product.domain.entity.QTimedealPolicy;
import ktb.leafresh.backend.domain.store.product.domain.entity.enums.ProductStatus;
import ktb.leafresh.backend.domain.store.product.presentation.dto.response.TimedealProductSummaryResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TimedealProductQueryRepositoryImpl implements TimedealProductQueryRepository {

    private final JPAQueryFactory queryFactory;

    QTimedealPolicy tp = QTimedealPolicy.timedealPolicy;
    QProduct p = QProduct.product;

    @Override
    public List<TimedealProductSummaryResponseDto> findTimedeals(LocalDateTime now, LocalDateTime oneWeekLater) {
        return queryFactory
                .select(Projections.constructor(TimedealProductSummaryResponseDto.class,
                        tp.id,
                        p.id,
                        p.name,                     // title
                        p.description,
                        p.price,                    // defaultPrice
                        tp.discountedPrice,
                        tp.discountedPercentage,
                        tp.stock,
                        p.imageUrl,
                        tp.startTime,               // dealStartTime
                        tp.endTime,                 // dealEndTime
                        getProductStatusCase(),       // CASE WHEN p.status == SOLD_OUT THEN SOLD_OUT ELSE ACTIVE
                        getTimeDealStatusCase(now)    // CASE WHEN start < now AND end > now THEN ONGOING ELSE UPCOMING
                ))
                .from(tp)
                .join(tp.product, p)
                .where(
                        tp.deletedAt.isNull(),
                        p.deletedAt.isNull(),
                        p.status.in(ProductStatus.ACTIVE, ProductStatus.SOLD_OUT),
                        tp.startTime.between(now, oneWeekLater)
                                .or(tp.startTime.before(now).and(tp.endTime.after(now)))
                )
                .orderBy(tp.startTime.asc())
                .fetch();
    }

    private Expression<String> getProductStatusCase() {
        return new CaseBuilder()
                .when(p.status.eq(ProductStatus.SOLD_OUT)).then("SOLD_OUT")
                .otherwise("ACTIVE");
    }

    private Expression<String> getTimeDealStatusCase(LocalDateTime now) {
        return new CaseBuilder()
                .when(tp.startTime.before(now).and(tp.endTime.after(now))).then("ONGOING")
                .otherwise("UPCOMING");
    }

    @Override
    public List<TimedealProductSummaryResponseDto> findByIds(List<Long> ids) {
        LocalDateTime now = LocalDateTime.now();

        return queryFactory
                .select(Projections.constructor(TimedealProductSummaryResponseDto.class,
                        tp.id,
                        p.id,
                        p.name,
                        p.description,
                        p.price,
                        tp.discountedPrice,
                        tp.discountedPercentage,
                        tp.stock,
                        p.imageUrl,
                        tp.startTime,
                        tp.endTime,
                        getProductStatusCase(),
                        getTimeDealStatusCase(now)
                ))
                .from(tp)
                .join(tp.product, p)
                .where(
                        tp.deletedAt.isNull(),
                        p.deletedAt.isNull(),
                        p.status.in(ProductStatus.ACTIVE, ProductStatus.SOLD_OUT),
                        tp.id.in(ids)
                )
                .fetch();
    }
}
