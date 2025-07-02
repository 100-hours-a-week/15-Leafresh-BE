//package ktb.leafresh.backend.domain.store.order.application.service;
//
//import ktb.leafresh.backend.domain.member.domain.entity.Member;
//import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
//import ktb.leafresh.backend.domain.store.order.application.dto.PurchaseCommand;
//import ktb.leafresh.backend.domain.store.order.application.service.model.PurchaseProcessContext;
//import ktb.leafresh.backend.domain.store.order.domain.entity.PurchaseFailureLog;
//import ktb.leafresh.backend.domain.store.order.infrastructure.repository.PurchaseFailureLogRepository;
//import ktb.leafresh.backend.domain.store.product.domain.entity.Product;
//import ktb.leafresh.backend.domain.store.product.domain.entity.TimedealPolicy;
//import ktb.leafresh.backend.domain.store.product.infrastructure.repository.ProductRepository;
//import ktb.leafresh.backend.domain.store.product.infrastructure.repository.TimedealPolicyRepository;
//import ktb.leafresh.backend.global.exception.CustomException;
//import ktb.leafresh.backend.support.fixture.MemberFixture;
//import ktb.leafresh.backend.support.fixture.ProductFixture;
//import ktb.leafresh.backend.support.fixture.TimedealPolicyFixture;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ProductPurchaseProcessingServiceTest {
//
//    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2025, 7, 1, 12, 0);
//    private static final String RUNTIME_ERROR_MSG = "예상치 못한 오류";
//
//    @Mock
//    private MemberRepository memberRepository;
//
//    @Mock
//    private ProductRepository productRepository;
//
//    @Mock
//    private TimedealPolicyRepository timedealPolicyRepository;
//
//    @Mock
//    private PurchaseFailureLogRepository failureLogRepository;
//
//    @Mock
//    private PurchaseProcessor purchaseProcessor;
//
//    @InjectMocks
//    private ProductPurchaseProcessingService service;
//
//    private Member member;
//    private Product product;
//
//    @BeforeEach
//    void setUp() {
//        member = MemberFixture.of(1L, "test@leafresh.com", "테스터");
//        product = ProductFixture.createDefaultProduct();
//    }
//
//    @Test
//    @DisplayName("타임딜 없이 정상 주문 처리")
//    void process_withoutTimedeal_success() {
//        var command = new PurchaseCommand(
//                member.getId(), product.getId(), null, 2, "test-key", FIXED_TIME);
//
//        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
//        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
//
//        service.process(command);
//
//        verify(purchaseProcessor).process(any(PurchaseProcessContext.class));
//    }
//
//    @Test
//    @DisplayName("타임딜 ID가 주어졌을 때 타임딜 구매 처리")
//    void process_withTimedeal_success() {
//        TimedealPolicy timedeal = TimedealPolicyFixture.createDefaultTimedeal(product);
//        ReflectionTestUtils.setField(timedeal, "id", 100L); // 💡 ID 수동 설정
//
//        var command = new PurchaseCommand(
//                member.getId(), product.getId(), 100L, 1, "key", FIXED_TIME);
//
//        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
//        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
//        when(timedealPolicyRepository.findById(100L)).thenReturn(Optional.of(timedeal));
//
//        service.process(command);
//
//        verify(purchaseProcessor).process(any(PurchaseProcessContext.class));
//    }
//
//    @Test
//    @DisplayName("회원이 존재하지 않을 경우 예외 발생 및 실패 로그 저장")
//    void process_fail_memberNotFound() {
//        var command = new PurchaseCommand(
//                member.getId(), product.getId(), null, 1, "key", FIXED_TIME);
//
//        when(memberRepository.findById(member.getId())).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> service.process(command))
//                .isInstanceOf(CustomException.class);
//
//        verify(failureLogRepository).save(any(PurchaseFailureLog.class));
//    }
//
//    @Test
//    @DisplayName("상품이 존재하지 않을 경우 예외 발생 및 실패 로그 저장")
//    void process_fail_productNotFound() {
//        var command = new PurchaseCommand(
//                member.getId(), product.getId(), null, 1, "key", FIXED_TIME);
//
//        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
//        when(productRepository.findById(product.getId())).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> service.process(command))
//                .isInstanceOf(CustomException.class);
//
//        verify(failureLogRepository).save(any(PurchaseFailureLog.class));
//    }
//
//    @Test
//    @DisplayName("예상치 못한 예외 발생 시 실패 로그 저장 및 메시지 확인")
//    void process_fail_byRuntimeException() {
//        var command = new PurchaseCommand(
//                member.getId(), product.getId(), null, 1, "key", FIXED_TIME);
//
//        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
//        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
//        doThrow(new RuntimeException(RUNTIME_ERROR_MSG)).when(purchaseProcessor).process(any());
//
//        Throwable exception = catchThrowable(() -> service.process(command));
//
//        assertThat(exception)
//                .isInstanceOf(RuntimeException.class)
//                .hasMessage(RUNTIME_ERROR_MSG);
//
//        verify(failureLogRepository).save(any(PurchaseFailureLog.class));
//    }
//}
