package ktb.leafresh.backend.domain.feedback.application.service;

import ktb.leafresh.backend.domain.feedback.domain.entity.Feedback;
import ktb.leafresh.backend.domain.feedback.infrastructure.repository.FeedbackRepository;
import ktb.leafresh.backend.domain.feedback.presentation.dto.response.FeedbackResponseDto;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.support.fixture.FeedbackFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FeedbackResultQueryServiceTest {

    private FeedbackResultQueryService service;
    private MemberRepository memberRepository;
    private FeedbackRepository feedbackRepository;
    private RedisTemplate<String, Object> redisTemplate;
    private ValueOperations<String, Object> valueOperations;

    private final Long memberId = 1L;
    private Member member;

    @BeforeEach
    void setUp() {
        member = MemberFixture.of(memberId, "test@leafresh.com", "테스터");

        memberRepository = mock(MemberRepository.class);
        feedbackRepository = mock(FeedbackRepository.class);
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service = new FeedbackResultQueryService(
                memberRepository,
                feedbackRepository,
                redisTemplate
        );
    }

    @Test
    @DisplayName("Redis 캐시에 피드백이 있으면 해당 값을 반환한다")
    void getFeedbackResult_cached() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(valueOperations.get("feedback:result:1")).thenReturn("캐시된 피드백");

        FeedbackResponseDto result = service.getFeedbackResult(memberId);
        assertThat(result.getContent()).isEqualTo("캐시된 피드백");
    }

    @Test
    @DisplayName("Redis 캐시에 없고 DB에도 피드백이 없으면 null을 포함한 응답을 반환한다")
    void getFeedbackResult_notFound() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(valueOperations.get("feedback:result:1")).thenReturn(null);
        when(feedbackRepository.findFeedbackByMemberAndWeekMonday(eq(member), any()))
                .thenReturn(Optional.empty());

        FeedbackResponseDto result = service.getFeedbackResult(memberId);
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNull();
    }

    @Test
    @DisplayName("DB에서 피드백이 조회되면 해당 값을 반환한다")
    void getFeedbackResult_fromDb() {
        Feedback feedback = FeedbackFixture.of(member, "DB 피드백");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(valueOperations.get("feedback:result:1")).thenReturn(null);
        when(feedbackRepository.findFeedbackByMemberAndWeekMonday(eq(member), any()))
                .thenReturn(Optional.of(feedback));

        FeedbackResponseDto result = service.getFeedbackResult(memberId);
        assertThat(result.getContent()).isEqualTo("DB 피드백");
    }

    @Test
    @DisplayName("존재하지 않는 멤버라면 UNAUTHORIZED 예외를 던진다")
    void getFeedbackResult_noMember() {
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        CustomException ex = catchThrowableOfType(() -> service.getFeedbackResult(memberId), CustomException.class);
        assertThat(ex.getErrorCode()).isEqualTo(GlobalErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("비활성화된 멤버라면 ACCESS_DENIED 예외를 던진다")
    void getFeedbackResult_notActivated() {
        Member inactiveMember = MemberFixture.of(memberId, "inactive@leafresh.com", "비활성");
        ReflectionTestUtils.setField(inactiveMember, "activated", false);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(inactiveMember));

        CustomException ex = catchThrowableOfType(() -> service.getFeedbackResult(memberId), CustomException.class);
        assertThat(ex.getErrorCode()).isEqualTo(GlobalErrorCode.ACCESS_DENIED);
    }
}
