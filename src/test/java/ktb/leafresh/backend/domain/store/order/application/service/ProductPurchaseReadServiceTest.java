package ktb.leafresh.backend.domain.store.order.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.store.order.domain.entity.ProductPurchase;
import ktb.leafresh.backend.domain.store.order.infrastructure.repository.ProductPurchaseQueryRepository;
import ktb.leafresh.backend.domain.store.order.presentation.dto.response.ProductPurchaseListResponseDto;
import ktb.leafresh.backend.domain.store.order.presentation.dto.response.ProductPurchaseSummaryResponseDto;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import ktb.leafresh.backend.support.fixture.ProductFixture;
import ktb.leafresh.backend.support.fixture.ProductPurchaseFixture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ProductPurchaseReadServiceTest {

    @Mock
    private ProductPurchaseQueryRepository productPurchaseQueryRepository;

    @InjectMocks
    private ProductPurchaseReadService productPurchaseReadService;

    private Member member;
    private ProductPurchase purchase;

    private static final Long MEMBER_ID = 1L;
    private static final String INPUT = "";
    private static final Long CURSOR_ID = null;
    private static final String CURSOR_TIMESTAMP = null;
    private static final int SIZE = 10;

    @BeforeEach
    void setUp() {
        member = MemberFixture.of();
        var product = ProductFixture.createDefaultProduct();

        purchase = ProductPurchaseFixture.create(member, product);
        ReflectionTestUtils.setField(purchase, "id", 999L);
        ReflectionTestUtils.setField(product, "id", 555L);
    }

    @Test
    @DisplayName("회원의 상품 구매 내역을 커서 기반으로 조회한다")
    void getPurchases_success() {
        // given
        given(productPurchaseQueryRepository.findByMemberWithCursorAndSearch(
                MEMBER_ID, INPUT, CURSOR_ID, CURSOR_TIMESTAMP, SIZE)
        ).willReturn(List.of(purchase));

        // when
        ProductPurchaseListResponseDto result = productPurchaseReadService.getPurchases(
                MEMBER_ID, INPUT, CURSOR_ID, CURSOR_TIMESTAMP, SIZE
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPurchases()).hasSize(1);

        ProductPurchaseSummaryResponseDto dto = result.getPurchases().get(0);

        assertThat(dto.getId()).isEqualTo(purchase.getId());
        assertThat(dto.getQuantity()).isEqualTo(purchase.getQuantity());
        assertThat(dto.getPrice()).isEqualTo(purchase.getPrice());

        assertThat(dto.getProduct().getId()).isEqualTo(purchase.getProduct().getId());
        assertThat(dto.getProduct().getTitle()).isEqualTo(purchase.getProduct().getName());
        assertThat(dto.getProduct().getImageUrl()).isEqualTo(purchase.getProduct().getImageUrl());

        assertThat(dto.getPurchasedAt().toLocalDateTime()).isEqualTo(purchase.getPurchasedAt());

        assertThat(result.isHasNext()).isFalse(); // 단일 결과만 반환했으므로 false
        assertThat(result.getCursorInfo()).isNotNull(); // 커서 정보 존재 여부만 확인
    }
}
