package ktb.leafresh.backend.domain.feedback.application.service;

import ktb.leafresh.backend.domain.feedback.domain.entity.Feedback;
import ktb.leafresh.backend.domain.feedback.infrastructure.repository.FeedbackRepository;
import ktb.leafresh.backend.domain.feedback.presentation.dto.response.FeedbackResponseDto;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.FeedbackErrorCode;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.util.polling.FeedbackPollingExecutor;
import ktb.leafresh.backend.support.fixture.FeedbackFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FeedbackResultQueryServiceTest {

    private FeedbackResultQueryService service;
    private MemberRepository memberRepository;
    private FeedbackRepository feedbackRepository;
    private FeedbackPollingExecutor feedbackPollingExecutor;
    private RedisTemplate<String, Object> redisTemplate;
    private ValueOperations<String, Object> valueOperations;

    private final Long memberId = 1L;
    private Member member;

    @BeforeEach
    void setUp() {
        // fixture를 통해 활성화된 member 생성
        member = MemberFixture.of(memberId, "test@leafresh.com", "테스터");

        memberRepository = mock(MemberRepository.class);
        feedbackRepository = mock(FeedbackRepository.class);
        feedbackPollingExecutor = mock(FeedbackPollingExecutor.class);
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service = new FeedbackResultQueryService(
                memberRepository,
                feedbackRepository,
                feedbackPollingExecutor,
                redisTemplate
        );
    }

    @Test
    @DisplayName("Redis 캐시에 피드백이 있으면 해당 값을 반환한다")
    void waitForFeedback_cached() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(valueOperations.get("feedback:result:1")).thenReturn("캐시된 피드백");

        when(feedbackPollingExecutor.poll(any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<FeedbackResponseDto> supplier = invocation.getArgument(0);
                    return supplier.get();
                });

        FeedbackResponseDto result = service.waitForFeedback(memberId);
        assertThat(result.getContent()).isEqualTo("캐시된 피드백");
    }

    @Test
    @DisplayName("Redis 캐시에 없고 DB에도 피드백이 없으면 null을 포함한 응답을 반환한다")
    void waitForFeedback_notReady() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(valueOperations.get("feedback:result:1")).thenReturn(null);
        when(feedbackRepository.findFeedbackByMemberAndWeekMonday(eq(member), any()))
                .thenReturn(Optional.empty());

        when(feedbackPollingExecutor.poll(any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<FeedbackResponseDto> supplier = invocation.getArgument(0);
                    return supplier.get();
                });

        FeedbackResponseDto result = service.waitForFeedback(memberId);
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNull();
    }

    @Test
    @DisplayName("DB에서 피드백이 조회되면 해당 값을 반환한다")
    void waitForFeedback_fromDb() {
        Feedback feedback = FeedbackFixture.of(member, "DB 피드백");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(valueOperations.get("feedback:result:1")).thenReturn(null);
        when(feedbackRepository.findFeedbackByMemberAndWeekMonday(eq(member), any()))
                .thenReturn(Optional.of(feedback));

        when(feedbackPollingExecutor.poll(any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<FeedbackResponseDto> supplier = invocation.getArgument(0);
                    return supplier.get();
                });

        FeedbackResponseDto result = service.waitForFeedback(memberId);
        assertThat(result.getContent()).isEqualTo("DB 피드백");
    }

    @Test
    @DisplayName("존재하지 않는 멤버라면 UNAUTHORIZED 예외를 던진다")
    void waitForFeedback_noMember() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        CustomException ex = catchThrowableOfType(() -> service.waitForFeedback(memberId), CustomException.class);
        assertThat(ex.getErrorCode()).isEqualTo(GlobalErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("비활성화된 멤버라면 ACCESS_DENIED 예외를 던진다")
    void waitForFeedback_notActivated() {
        // 비활성화된 멤버 fixture 생성
        Member inactiveMember = MemberFixture.of(memberId, "inactive@leafresh.com", "비활성");
        ReflectionTestUtils.setField(inactiveMember, "activated", false);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(inactiveMember));

        CustomException ex = catchThrowableOfType(() -> service.waitForFeedback(memberId), CustomException.class);
        assertThat(ex.getErrorCode()).isEqualTo(GlobalErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("poll 도중 예외가 발생하면 FEEDBACK_SERVER_ERROR 예외를 던진다")
    void waitForFeedback_pollingFailure() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(feedbackPollingExecutor.poll(any(Supplier.class)))
                .thenThrow(new RuntimeException("서버 내부 오류"));

        CustomException ex = catchThrowableOfType(() -> service.waitForFeedback(memberId), CustomException.class);
        assertThat(ex.getErrorCode()).isEqualTo(FeedbackErrorCode.FEEDBACK_SERVER_ERROR);
    }
}
