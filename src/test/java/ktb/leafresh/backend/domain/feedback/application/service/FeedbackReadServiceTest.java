//package ktb.leafresh.backend.domain.feedback.application.service;
//
//import ktb.leafresh.backend.domain.feedback.domain.entity.Feedback;
//import ktb.leafresh.backend.domain.feedback.infrastructure.repository.FeedbackRepository;
//import ktb.leafresh.backend.domain.feedback.presentation.dto.response.FeedbackResponseDto;
//import ktb.leafresh.backend.domain.member.domain.entity.Member;
//import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
//import ktb.leafresh.backend.global.exception.CustomException;
//import ktb.leafresh.backend.global.exception.FeedbackErrorCode;
//import ktb.leafresh.backend.global.exception.GlobalErrorCode;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.Optional;
//import java.util.concurrent.TimeUnit;
//
//import static ktb.leafresh.backend.support.fixture.FeedbackFixture.of;
//import static ktb.leafresh.backend.support.fixture.MemberFixture.of;
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class FeedbackReadServiceTest {
//
//    private FeedbackRepository feedbackRepository;
//    private MemberRepository memberRepository;
//    private RedisTemplate<String, Object> redisTemplate;
//    private ValueOperations<String, Object> valueOperations;
//    private FeedbackReadService service;
//
//    private final Long memberId = 1L;
//    private Member member;
//
//    @BeforeEach
//    void setUp() {
//        feedbackRepository = mock(FeedbackRepository.class);
//        memberRepository = mock(MemberRepository.class);
//        redisTemplate = mock(RedisTemplate.class);
//        valueOperations = mock(ValueOperations.class);
//        member = of(memberId, "test@leafresh.com", "테스터");
//
//        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
//        service = new FeedbackReadService(feedbackRepository, memberRepository, redisTemplate);
//    }
//
//    @Test
//    @DisplayName("캐시 히트 시 DB 조회 없이 캐시된 값을 반환한다")
//    void getFeedback_cached() {
//        // given
//        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
//        when(valueOperations.get("feedback:result:1")).thenReturn("캐시된 피드백");
//
//        // when
//        FeedbackResponseDto result = service.getFeedbackForLastWeek(memberId);
//
//        // then
//        assertThat(result.getContent()).isEqualTo("캐시된 피드백");
//        verify(feedbackRepository, never()).findFeedbackByMemberAndWeekMonday(any(), any());
//    }
//
////    @Test
////    @DisplayName("캐시 미스 시 DB 조회 후 Redis에 저장하고 값을 반환한다")
////    void getFeedback_fromDb() {
////        // given
////        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
////        when(valueOperations.get(anyString())).thenReturn(null);
////
////        LocalDateTime lastWeekMonday = LocalDate.now()
////                .with(DayOfWeek.MONDAY)
////                .minusWeeks(1)
////                .atStartOfDay();
////
////        Feedback feedback = of(member, "DB 피드백", lastWeekMonday);
////        when(feedbackRepository.findFeedbackByMemberAndWeekMonday(eq(member), eq(lastWeekMonday)))
////                .thenReturn(Optional.of(feedback));
////
////        // when
////        FeedbackResponseDto result = service.getFeedbackForLastWeek(memberId);
////
////        // then
////        assertThat(result.getContent()).isEqualTo("DB 피드백");
////
////        ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);
////        verify(valueOperations).set(
////                eq("feedback:result:1"),
////                eq("DB 피드백"),
////                ttlCaptor.capture(),
////                eq(TimeUnit.SECONDS)
////        );
////        assertThat(ttlCaptor.getValue()).isGreaterThan(0L);
////    }
//
//    @Test
//    @DisplayName("DB에 피드백이 없다면 null을 반환한다")
//    void getFeedback_empty() {
//        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
//        when(valueOperations.get(anyString())).thenReturn(null);
//
//        LocalDateTime lastWeekMonday = LocalDate.now().with(DayOfWeek.MONDAY).minusWeeks(1).atStartOfDay();
//        when(feedbackRepository.findFeedbackByMemberAndWeekMonday(eq(member), eq(lastWeekMonday)))
//                .thenReturn(Optional.empty());
//
//        FeedbackResponseDto result = service.getFeedbackForLastWeek(memberId);
//
//        assertThat(result.getContent()).isNull();
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 멤버일 경우 UNAUTHORIZED 예외 발생")
//    void getFeedback_noMember() {
//        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());
//
//        CustomException ex = catchThrowableOfType(
//                () -> service.getFeedbackForLastWeek(memberId),
//                CustomException.class
//        );
//
//        assertThat(ex.getErrorCode()).isEqualTo(GlobalErrorCode.UNAUTHORIZED);
//    }
//
//    @Test
//    @DisplayName("DB 조회 중 예외 발생 시 FEEDBACK_SERVER_ERROR 반환")
//    void getFeedback_dbError() {
//        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
//        when(valueOperations.get(anyString())).thenReturn(null);
//        when(feedbackRepository.findFeedbackByMemberAndWeekMonday(any(), any()))
//                .thenThrow(new RuntimeException("DB 오류"));
//
//        CustomException ex = catchThrowableOfType(
//                () -> service.getFeedbackForLastWeek(memberId),
//                CustomException.class
//        );
//
//        assertThat(ex.getErrorCode()).isEqualTo(FeedbackErrorCode.FEEDBACK_SERVER_ERROR);
//    }
//}
