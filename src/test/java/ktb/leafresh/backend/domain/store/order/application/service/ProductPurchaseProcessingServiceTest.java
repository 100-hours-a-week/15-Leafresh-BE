package ktb.leafresh.backend.domain.store.order.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.store.order.application.dto.PurchaseCommand;
import ktb.leafresh.backend.domain.store.order.application.service.model.PurchaseProcessContext;
import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseFailureLog;
import ktb.leafresh.backend.domain.store.order.infrastructure.repository.PurchaseFailureLogRepository;
import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
import ktb.leafresh.backend.domain.store.product.infrastructure.repository.TimedealPolicyRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import ktb.leafresh.backend.support.fixture.ProductFixture;
import ktb.leafresh.backend.support.fixture.TimedealPolicyFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductPurchaseProcessingServiceTest {

    private MemberRepository memberRepository;
    private ProductRepository productRepository;
    private TimedealPolicyRepository timedealPolicyRepository;
    private PurchaseFailureLogRepository failureLogRepository;
    private PurchaseProcessor purchaseProcessor;

    private ProductPurchaseProcessingService service;

    private Member member;
    private Product product;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        productRepository = mock(ProductRepository.class);
        timedealPolicyRepository = mock(TimedealPolicyRepository.class);
        failureLogRepository = mock(PurchaseFailureLogRepository.class);
        purchaseProcessor = mock(PurchaseProcessor.class);

        service = new ProductPurchaseProcessingService(
                memberRepository,
                productRepository,
                timedealPolicyRepository,
                failureLogRepository,
                purchaseProcessor
        );

        member = MemberFixture.of(1L, "test@leafresh.com", "테스터");
        product = ProductFixture.of("주방세제", 3000, 100);
    }

    @Test
    @DisplayName("타임딜 없이 정상 주문 처리")
    void process_withoutTimedeal_success() {
        var command = new PurchaseCommand(1L, 1L, null, 2, "test-key", LocalDateTime.now());

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        service.process(command);

        verify(purchaseProcessor).process(any(PurchaseProcessContext.class));
    }

    @Test
    @DisplayName("타임딜 ID가 주어졌을 때 타임딜 구매 처리")
    void process_withTimedeal_success() {
        TimedealPolicy timedeal = TimedealPolicyFixture.of(product);
        var command = new PurchaseCommand(1L, 1L, timedeal.getId(), 1, "key", LocalDateTime.now());

        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(timedealPolicyRepository.findById(timedeal.getId())).thenReturn(Optional.of(timedeal));

        service.process(command);

        verify(purchaseProcessor).process(any(PurchaseProcessContext.class));
    }

    @Test
    @DisplayName("회원이 존재하지 않을 경우 예외 발생 및 실패 로그 저장")
    void process_fail_memberNotFound() {
        var command = new PurchaseCommand(1L, 1L, null, 1, "key", LocalDateTime.now());
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.process(command))
                .isInstanceOf(CustomException.class);

        verify(failureLogRepository).save(any(PurchaseFailureLog.class));
    }

    @Test
    @DisplayName("상품이 존재하지 않을 경우 예외 발생 및 실패 로그 저장")
    void process_fail_productNotFound() {
        var command = new PurchaseCommand(1L, 1L, null, 1, "key", LocalDateTime.now());
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.process(command))
                .isInstanceOf(CustomException.class);

        verify(failureLogRepository).save(any(PurchaseFailureLog.class));
    }

    @Test
    @DisplayName("예상치 못한 예외 발생 시 실패 로그 저장")
    void process_fail_byRuntimeException() {
        var command = new PurchaseCommand(1L, 1L, null, 1, "key", LocalDateTime.now());
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doThrow(new RuntimeException("예상치 못한 오류")).when(purchaseProcessor).process(any());

        assertThatThrownBy(() -> service.process(command))
                .isInstanceOf(RuntimeException.class);

        verify(failureLogRepository).save(any(PurchaseFailureLog.class));
    }
}
