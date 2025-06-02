package ktb.leafresh.backend.domain.store.order.infrastructure.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import ktb.leafresh.backend.domain.store.order.domain.entity.ProductPurchase;
import ktb.leafresh.backend.domain.store.order.domain.entity.QProductPurchase;
import ktb.leafresh.backend.domain.store.product.domain.entity.QProduct;
import ktb.leafresh.backend.global.util.pagination.CursorConditionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductPurchaseQueryRepositoryImpl implements ProductPurchaseQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QProductPurchase purchase = QProductPurchase.productPurchase;
    private final QProduct product = QProduct.product;

    @Override
    public List<ProductPurchase> findByMemberWithCursorAndSearch(Long memberId, String input, Long cursorId, String cursorTimestamp, int size) {
        LocalDateTime ts = CursorConditionUtils.parseTimestamp(cursorTimestamp);

        return queryFactory
                .selectFrom(purchase)
                .join(purchase.product, product).fetchJoin()
                .where(
                        purchase.member.id.eq(memberId),
                        CursorConditionUtils.ltCursorWithTimestamp(purchase.purchasedAt, purchase.id, ts, cursorId),
                        likeInput(input)
                )
                .orderBy(purchase.purchasedAt.desc(), purchase.id.desc())
                .limit(size + 1)
                .fetch();
    }

    private BooleanExpression likeInput(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        return product.name.containsIgnoreCase(input)
                .or(product.description.containsIgnoreCase(input));
    }
}
