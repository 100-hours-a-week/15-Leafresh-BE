//package ktb.leafresh.backend.domain.verification.application.service;
//
//import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
//import ktb.leafresh.backend.domain.challenge.personal.infrastructure.repository.PersonalChallengeRepository;
//import ktb.leafresh.backend.domain.member.domain.entity.Member;
//import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
//import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;
//import ktb.leafresh.backend.domain.verification.domain.event.VerificationCreatedEvent;
//import ktb.leafresh.backend.domain.verification.domain.support.validator.VerificationSubmitValidator;
//import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
//import ktb.leafresh.backend.domain.verification.presentation.dto.request.PersonalChallengeVerificationRequestDto;
//import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
//import ktb.leafresh.backend.global.exception.CustomException;
//import ktb.leafresh.backend.global.exception.MemberErrorCode;
//import ktb.leafresh.backend.global.exception.VerificationErrorCode;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//
//import java.util.Optional;
//
//import static ktb.leafresh.backend.support.fixture.MemberFixture.of;
//import static ktb.leafresh.backend.support.fixture.PersonalChallengeFixture.of;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.Mockito.*;
//
//class PersonalChallengeVerificationSubmitServiceTest {
//
//    private MemberRepository memberRepository;
//    private PersonalChallengeRepository challengeRepository;
//    private PersonalChallengeVerificationRepository verificationRepository;
//    private VerificationSubmitValidator validator;
//    private ApplicationEventPublisher eventPublisher;
//    private StringRedisTemplate redisTemplate;
//    private ValueOperations<String, String> valueOps;
//    private PersonalChallengeVerificationSubmitService service;
//
//    private Member member;
//    private PersonalChallenge challenge;
//
//    @BeforeEach
//    void setUp() {
//        memberRepository = mock(MemberRepository.class);
//        challengeRepository = mock(PersonalChallengeRepository.class);
//        verificationRepository = mock(PersonalChallengeVerificationRepository.class);
//        validator = mock(VerificationSubmitValidator.class);
//        eventPublisher = mock(ApplicationEventPublisher.class);
//        redisTemplate = mock(StringRedisTemplate.class);
//        valueOps = mock(ValueOperations.class);
//        when(redisTemplate.opsForValue()).thenReturn(valueOps);
//
//        service = new PersonalChallengeVerificationSubmitService(
//                memberRepository, challengeRepository, verificationRepository,
//                validator, eventPublisher, redisTemplate
//        );
//
//        member = of(1L, "test@leafresh.com", "테스터");
//        challenge = of("제로웨이스트 챌린지");
//    }
//
//    @Test
//    @DisplayName("개인 인증을 정상적으로 제출한다")
//    void submit_success() {
//        var dto = new PersonalChallengeVerificationRequestDto("https://img.jpg", "잘 인증했어요");
//
//        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
//        when(challengeRepository.findById(1L)).thenReturn(Optional.of(challenge));
//        when(verificationRepository.findTopByMemberIdAndPersonalChallengeIdAndCreatedAtBetween(
//                eq(1L), eq(1L), any(), any())).thenReturn(Optional.empty());
//
//        service.submit(1L, 1L, dto);
//
//        verify(validator).validate("잘 인증했어요");
//        verify(verificationRepository).save(any(PersonalChallengeVerification.class));
//        verify(eventPublisher).publishEvent(any(VerificationCreatedEvent.class));
//        verify(valueOps).increment("leafresh:totalVerifications:count"); // ✅ 변경됨
//    }
//
//    @Test
//    @DisplayName("이미 오늘 인증한 경우 예외 발생")
//    void submit_fail_alreadySubmitted() {
//        var dto = new PersonalChallengeVerificationRequestDto("https://img.jpg", "중복 인증");
//        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
//        when(challengeRepository.findById(1L)).thenReturn(Optional.of(challenge));
//        when(verificationRepository.findTopByMemberIdAndPersonalChallengeIdAndCreatedAtBetween(
//                eq(1L), eq(1L), any(), any())).thenReturn(Optional.of(mock(PersonalChallengeVerification.class)));
//
//        assertThatThrownBy(() -> service.submit(1L, 1L, dto))
//                .isInstanceOf(CustomException.class)
//                .hasMessageContaining(VerificationErrorCode.ALREADY_SUBMITTED.getMessage());
//    }
//
//    @Test
//    @DisplayName("회원이 존재하지 않으면 예외 발생")
//    void submit_fail_memberNotFound() {
//        var dto = new PersonalChallengeVerificationRequestDto("https://img.jpg", "인증");
//        when(memberRepository.findById(1L)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> service.submit(1L, 1L, dto))
//                .isInstanceOf(CustomException.class)
//                .hasMessageContaining(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
//    }
//
//    @Test
//    @DisplayName("챌린지를 찾을 수 없으면 예외 발생")
//    void submit_fail_challengeNotFound() {
//        var dto = new PersonalChallengeVerificationRequestDto("https://img.jpg", "인증");
//        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
//        when(challengeRepository.findById(1L)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> service.submit(1L, 1L, dto))
//                .isInstanceOf(CustomException.class)
//                .hasMessageContaining(ChallengeErrorCode.PERSONAL_CHALLENGE_NOT_FOUND.getMessage());
//    }
//
//    @Test
//    @DisplayName("AI 이벤트 발행 중 예외 발생 시 AI_SERVER_ERROR 예외 반환")
//    void submit_fail_aiServerError() {
//        var dto = new PersonalChallengeVerificationRequestDto("https://img.jpg", "인증");
//        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
//        when(challengeRepository.findById(1L)).thenReturn(Optional.of(challenge));
//        when(verificationRepository.findTopByMemberIdAndPersonalChallengeIdAndCreatedAtBetween(
//                anyLong(), anyLong(), any(), any())).thenReturn(Optional.empty());
//
//        doThrow(new RuntimeException("MQ Error"))
//                .when(eventPublisher).publishEvent(any(VerificationCreatedEvent.class));
//
//        assertThatThrownBy(() -> service.submit(1L, 1L, dto))
//                .isInstanceOf(CustomException.class)
//                .hasMessageContaining(VerificationErrorCode.AI_SERVER_ERROR.getMessage());
//    }
//}
