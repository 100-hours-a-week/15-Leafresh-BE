package ktb.leafresh.backend.domain.store.product.infrastructure.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.QProduct;
import ktb.leafresh.backend.domain.store.product.domain.entity.enums.ProductStatus;
import ktb.leafresh.backend.global.util.pagination.CursorConditionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductSearchQueryRepositoryImpl implements ProductSearchQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QProduct p = QProduct.product;

    @Override
    public List<Product> findWithFilter(String input, Long cursorId, String cursorTimestamp, int size) {
        LocalDateTime ts = CursorConditionUtils.parseTimestamp(cursorTimestamp);

        return queryFactory
                .selectFrom(p)
                .where(
                        p.deletedAt.isNull(),
                        p.status.in(ProductStatus.ACTIVE, ProductStatus.SOLD_OUT),
                        likeInput(input),
                        CursorConditionUtils.ltCursorWithTimestamp(p.createdAt, p.id, ts, cursorId)
                )
                .orderBy(p.createdAt.desc(), p.id.desc())
                .limit(size + 1)
                .fetch();
    }

    private BooleanExpression likeInput(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        return p.name.containsIgnoreCase(input).or(p.description.containsIgnoreCase(input));
    }
}
