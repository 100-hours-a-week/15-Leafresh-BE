package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.challenge.personal.infrastructure.repository.PersonalChallengeRepository;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;
import ktb.leafresh.backend.domain.verification.domain.event.VerificationCreatedEvent;
import ktb.leafresh.backend.domain.verification.domain.support.validator.VerificationSubmitValidator;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.PersonalChallengeVerificationRequestDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonalChallengeVerificationSubmitServiceTest {

  @Mock private MemberRepository memberRepository;
  @Mock private PersonalChallengeRepository challengeRepository;
  @Mock private PersonalChallengeVerificationRepository verificationRepository;
  @Mock private VerificationSubmitValidator validator;
  @Mock private ApplicationEventPublisher eventPublisher;
  @Mock private StringRedisTemplate redisTemplate;
  @Mock private ValueOperations<String, String> valueOperations;

  @InjectMocks private PersonalChallengeVerificationSubmitService submitService;

  private final Long memberId = 1L;
  private final Long challengeId = 10L;

  private Member member;
  private PersonalChallenge challenge;
  private PersonalChallengeVerificationRequestDto dto;

  @BeforeEach
  void setUp() {
    member = Member.builder().nickname("test").email("test@email.com").build();
    challenge = PersonalChallenge.builder().title("환경지키기").description("비닐 사용 줄이기").build();
    dto = new PersonalChallengeVerificationRequestDto("https://image.jpg", "비닐봉투 대신 장바구니 사용!");
  }

  @Test
  @DisplayName("정상 제출 시 - 인증 저장 및 이벤트 발행, Redis 증가")
  void submit_success() {
    // given
    given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
    given(challengeRepository.findById(challengeId)).willReturn(Optional.of(challenge));
    given(
            verificationRepository.findTopByMemberIdAndPersonalChallengeIdAndCreatedAtBetween(
                eq(memberId), eq(challengeId), any(LocalDateTime.class), any(LocalDateTime.class)))
        .willReturn(Optional.empty());
    given(verificationRepository.save(any()))
        .willAnswer(
            invocation -> {
              PersonalChallengeVerification saved = invocation.getArgument(0);
              ReflectionTestUtils.setField(saved, "id", 123L);
              return saved;
            });
    given(redisTemplate.opsForValue()).willReturn(valueOperations);

    // when
    submitService.submit(memberId, challengeId, dto);

    // then
    verify(validator).validate(dto.content());
    verify(verificationRepository).save(any(PersonalChallengeVerification.class));
    verify(eventPublisher).publishEvent(any(VerificationCreatedEvent.class));
    verify(redisTemplate.opsForValue()).increment("leafresh:totalVerifications:count");
  }

  @Test
  @DisplayName("이미 인증 제출한 경우 - 예외 발생")
  void submit_alreadySubmitted_throwsException() {
    // given
    given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
    given(challengeRepository.findById(challengeId)).willReturn(Optional.of(challenge));
    given(
            verificationRepository.findTopByMemberIdAndPersonalChallengeIdAndCreatedAtBetween(
                eq(memberId), eq(challengeId), any(), any()))
        .willReturn(Optional.of(mock(PersonalChallengeVerification.class)));

    // when & then
    assertThatThrownBy(() -> submitService.submit(memberId, challengeId, dto))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(VerificationErrorCode.ALREADY_SUBMITTED.getMessage());
  }
}
