// package ktb.leafresh.backend.domain.feedback.application.service;
//
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.databind.SerializationFeature;
// import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// import ktb.leafresh.backend.domain.feedback.application.assembler.FeedbackDtoAssembler;
// import ktb.leafresh.backend.domain.feedback.infrastructure.client.FeedbackCreationClient;
// import
// ktb.leafresh.backend.domain.feedback.infrastructure.dto.request.AiFeedbackCreationRequestDto;
// import ktb.leafresh.backend.domain.member.domain.entity.Member;
// import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
// import ktb.leafresh.backend.global.exception.CustomException;
// import ktb.leafresh.backend.global.exception.FeedbackErrorCode;
// import ktb.leafresh.backend.global.exception.GlobalErrorCode;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
//
// import java.util.List;
// import java.util.Optional;
//
// import static java.time.LocalDateTime.now;
// import static org.assertj.core.api.Assertions.*;
// import static org.mockito.Mockito.*;
//
// class FeedbackCommandServiceTest {
//
//    private MemberRepository memberRepository;
//    private FeedbackDtoAssembler dtoAssembler;
//    private FeedbackCreationClient feedbackCreationClient;
//    private ObjectMapper objectMapper;
//    private FeedbackCommandService feedbackCommandService;
//
//    @BeforeEach
//    void setUp() {
//        memberRepository = mock(MemberRepository.class);
//        dtoAssembler = mock(FeedbackDtoAssembler.class);
//        feedbackCreationClient = mock(FeedbackCreationClient.class);
//
//        objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule()); // 추가
//        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO 포맷 사용
//        feedbackCommandService = new FeedbackCommandService(
//                memberRepository, dtoAssembler, feedbackCreationClient, objectMapper
//        );
//    }
//
//    @Test
//    @DisplayName("정상적인 피드백 요청이 성공적으로 수행된다")
//    void handleFeedbackCreationRequest_success() {
//        // given
//        Long memberId = 1L;
//        Member member = mock(Member.class);
//        when(member.getActivated()).thenReturn(true);
//        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
//
//        AiFeedbackCreationRequestDto requestDto = AiFeedbackCreationRequestDto.builder()
//                .memberId(memberId)
//                .personalChallenges(List.of(
//                        AiFeedbackCreationRequestDto.PersonalChallengeDto.builder()
//                                .id(1L).title("걷기").isSuccess(true).build()
//                ))
//                .groupChallenges(List.of(
//                        AiFeedbackCreationRequestDto.GroupChallengeDto.builder()
//                                .id(1L)
//                                .title("환경 캠페인")
//                                .startDate(now())
//                                .endDate(now())
//                                .submissions(List.of(
//
// AiFeedbackCreationRequestDto.GroupChallengeDto.SubmissionDto.builder()
//                                                .isSuccess(true).submittedAt(now()).build()
//                                ))
//                                .build()
//                ))
//                .build();
//
//        when(dtoAssembler.assemble(anyLong(), any(), any())).thenReturn(requestDto);
//
//        // when & then
//        assertThatCode(() ->
//                feedbackCommandService.handleFeedbackCreationRequest(memberId)
//        ).doesNotThrowAnyException();
//
//        verify(feedbackCreationClient).requestWeeklyFeedback(eq(requestDto));
//    }
//
//    @Test
//    @DisplayName("개인 + 그룹 인증 수가 0이면 예외가 발생한다")
//    void handleFeedbackCreationRequest_fail_noActivity() {
//        // given
//        Long memberId = 1L;
//        Member member = mock(Member.class);
//        when(member.getActivated()).thenReturn(true);
//        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
//
//        AiFeedbackCreationRequestDto requestDto = AiFeedbackCreationRequestDto.builder()
//                .memberId(memberId)
//                .personalChallenges(List.of())
//                .groupChallenges(List.of())
//                .build();
//
//        when(dtoAssembler.assemble(anyLong(), any(), any())).thenReturn(requestDto);
//
//        // when
//        CustomException ex = catchThrowableOfType(
//                () -> feedbackCommandService.handleFeedbackCreationRequest(memberId),
//                CustomException.class
//        );
//
//        // then
//        assertThat(ex).isNotNull();
//        assertThat(ex.getErrorCode()).isEqualTo(FeedbackErrorCode.NO_CHALLENGE_ACTIVITY);
//        verify(feedbackCreationClient, never()).requestWeeklyFeedback(any());
//    }
//
//    @Test
//    @DisplayName("회원이 존재하지 않거나 비활성화 상태이면 예외가 발생한다")
//    void handleFeedbackCreationRequest_fail_invalidMember() {
//        // given
//        Long memberId = 99L;
//        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());
//
//        // when
//        CustomException ex = catchThrowableOfType(
//                () -> feedbackCommandService.handleFeedbackCreationRequest(memberId),
//                CustomException.class
//        );
//
//        // then
//        assertThat(ex).isNotNull();
//        assertThat(ex.getErrorCode()).isEqualTo(GlobalErrorCode.UNAUTHORIZED);
//    }
//
//    @Test
//    @DisplayName("ObjectMapper JSON 변환 실패 시 RuntimeException이 발생한다")
//    void handleFeedbackCreationRequest_fail_jsonError() throws Exception {
//        // given
//        Long memberId = 1L;
//        Member member = mock(Member.class);
//        when(member.getActivated()).thenReturn(true);
//        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
//
//        AiFeedbackCreationRequestDto requestDto = AiFeedbackCreationRequestDto.builder()
//                .memberId(memberId)
//                .personalChallenges(List.of(
//                        AiFeedbackCreationRequestDto.PersonalChallengeDto.builder()
//                                .id(1L).title("걷기").isSuccess(true).build()
//                ))
//                .groupChallenges(List.of())
//                .build();
//
//        when(dtoAssembler.assemble(anyLong(), any(), any())).thenReturn(requestDto);
//
//        // mock ObjectMapper 체이닝 지원 설정
//        ObjectMapper brokenObjectMapper = mock(ObjectMapper.class);
//        when(brokenObjectMapper.copy()).thenReturn(brokenObjectMapper);
//
// when(brokenObjectMapper.enable(SerializationFeature.INDENT_OUTPUT)).thenReturn(brokenObjectMapper);
//        when(brokenObjectMapper.writeValueAsString(any()))
//                .thenThrow(new JsonProcessingException("Mocked JSON 변환 실패") {});
//
//        FeedbackCommandService serviceWithBrokenMapper = new FeedbackCommandService(
//                memberRepository, dtoAssembler, feedbackCreationClient, brokenObjectMapper
//        );
//
//        // when
//        RuntimeException ex = catchThrowableOfType(
//                () -> serviceWithBrokenMapper.handleFeedbackCreationRequest(memberId),
//                RuntimeException.class
//        );
//
//        // then
//        assertThat(ex).isNotNull();
//        assertThat(ex.getMessage()).contains("JSON 직렬화 실패");
//    }
// }
