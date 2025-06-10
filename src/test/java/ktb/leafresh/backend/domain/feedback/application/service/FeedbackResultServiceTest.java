//package ktb.leafresh.backend.domain.feedback.application.service;
//
//import ktb.leafresh.backend.domain.feedback.domain.event.FeedbackCreatedEvent;
//import ktb.leafresh.backend.domain.feedback.infrastructure.repository.FeedbackRepository;
//import ktb.leafresh.backend.domain.feedback.presentation.dto.request.FeedbackResultRequestDto;
//import ktb.leafresh.backend.domain.member.domain.entity.Member;
//import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
//import ktb.leafresh.backend.global.exception.CustomException;
//import ktb.leafresh.backend.global.exception.FeedbackErrorCode;
//import ktb.leafresh.backend.global.exception.MemberErrorCode;
//import ktb.leafresh.backend.support.fixture.MemberFixture;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.context.ApplicationEventPublisher;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class FeedbackResultServiceTest {
//
//    private MemberRepository memberRepository;
//    private FeedbackRepository feedbackRepository;
//    private ApplicationEventPublisher eventPublisher;
//    private FeedbackResultService service;
//
//    @BeforeEach
//    void setUp() {
//        memberRepository = mock(MemberRepository.class);
//        feedbackRepository = mock(FeedbackRepository.class);
//        eventPublisher = mock(ApplicationEventPublisher.class);
//
//        service = new FeedbackResultService(memberRepository, feedbackRepository, eventPublisher);
//    }
//
//    @Test
//    @DisplayName("정상적인 피드백 요청이 오면 이벤트를 발행한다")
//    void receiveFeedback_success() {
//        // given
//        Member member = MemberFixture.of(1L, "test@leafresh.com", "테스터");
//        FeedbackResultRequestDto dto = new FeedbackResultRequestDto(member.getId(), "이번 주도 열심히 살았어요");
//
//        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
//        when(feedbackRepository.existsByMemberIdAndWeekMonday(eq(member.getId()), any())).thenReturn(false);
//
//        // when
//        service.receiveFeedback(dto);
//
//        // then
//        verify(eventPublisher, times(1)).publishEvent(
//                argThat(event ->
//                        event instanceof FeedbackCreatedEvent &&
//                                ((FeedbackCreatedEvent) event).memberId().equals(member.getId()) &&
//                                ((FeedbackCreatedEvent) event).content().equals(dto.content())
//                )
//        );
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 멤버라면 MEMBER_NOT_FOUND 예외를 던진다")
//    void receiveFeedback_noMember() {
//        // given
//        Long invalidMemberId = 99L;
//        FeedbackResultRequestDto dto = new FeedbackResultRequestDto(invalidMemberId, "feedback");
//
//        when(memberRepository.findById(invalidMemberId)).thenReturn(Optional.empty());
//
//        // when & then
//        CustomException ex = catchThrowableOfType(() -> service.receiveFeedback(dto), CustomException.class);
//        assertThat(ex.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
//    }
//
//    @Test
//    @DisplayName("이미 피드백이 존재한다면 ALREADY_FEEDBACK_EXISTS 예외를 던진다")
//    void receiveFeedback_alreadyExists() {
//        // given
//        Member member = MemberFixture.of(1L, "test@leafresh.com", "테스터");
//        FeedbackResultRequestDto dto = new FeedbackResultRequestDto(member.getId(), "이전 피드백 있음");
//
//        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
//        when(feedbackRepository.existsByMemberIdAndWeekMonday(eq(member.getId()), any())).thenReturn(true);
//
//        // when & then
//        CustomException ex = catchThrowableOfType(() -> service.receiveFeedback(dto), CustomException.class);
//        assertThat(ex.getErrorCode()).isEqualTo(FeedbackErrorCode.ALREADY_FEEDBACK_EXISTS);
//    }
//}
