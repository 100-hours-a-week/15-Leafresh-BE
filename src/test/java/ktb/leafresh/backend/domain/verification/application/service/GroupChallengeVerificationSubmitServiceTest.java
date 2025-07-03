package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeParticipantRecordRepository;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.domain.support.validator.VerificationSubmitValidator;
import ktb.leafresh.backend.domain.verification.infrastructure.publisher.GcpAiVerificationPubSubPublisher;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.GroupChallengeVerificationRequestDto;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import ktb.leafresh.backend.support.fixture.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@DisplayName("GroupChallengeVerificationSubmitService 테스트")
@ExtendWith(MockitoExtension.class)
class GroupChallengeVerificationSubmitServiceTest {

    @Mock
    private GroupChallengeRepository groupChallengeRepository;

    @Mock
    private GroupChallengeParticipantRecordRepository recordRepository;

    @Mock
    private GroupChallengeVerificationRepository verificationRepository;

    @Mock
    private VerificationSubmitValidator validator;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private GcpAiVerificationPubSubPublisher pubSubPublisher;

    @InjectMocks
    private GroupChallengeVerificationSubmitService submitService;

    private Member member;
    private GroupChallenge challenge;
    private GroupChallengeParticipantRecord participantRecord;
    private GroupChallengeVerificationRequestDto requestDto;

    @BeforeEach
    void setUp() {
        member = MemberFixture.of();
        challenge = GroupChallengeFixture.of(member, null);
        participantRecord = GroupChallengeParticipantRecordFixture.of(challenge, member);
        requestDto = new GroupChallengeVerificationRequestDto("https://img.test", "플라스틱 줄이기 캠페인 참여");
    }

    @Test
    @DisplayName("그룹 챌린지 인증 제출 성공")
    void submit_success() {
        // given
        Long memberId = 1L;
        Long challengeId = 1L;

        given(groupChallengeRepository.findById(challengeId)).willReturn(Optional.of(challenge));
        given(recordRepository.findByGroupChallengeIdAndMemberIdAndDeletedAtIsNull(challengeId, memberId))
                .willReturn(Optional.of(participantRecord));
        given(verificationRepository
                .findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdOrderByCreatedAtDesc(
                        memberId, challengeId)).willReturn(Optional.empty());
        given(verificationRepository.save(any())).willAnswer(invocation -> {
            GroupChallengeVerification saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 10L);
            return saved;
        });

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);

        // when
        submitService.submit(memberId, challengeId, requestDto);

        // then
        then(validator).should().validate(requestDto.content());
        then(groupChallengeRepository).should().findById(challengeId);
        then(recordRepository).should()
                .findByGroupChallengeIdAndMemberIdAndDeletedAtIsNull(challengeId, memberId);
        then(verificationRepository).should()
                .save(any(GroupChallengeVerification.class));
        then(pubSubPublisher).should().publishAsyncWithRetry(any());
        then(redisTemplate.opsForValue()).should().increment("leafresh:totalVerifications:count");
    }

    @Test
    @DisplayName("그룹 챌린지를 찾을 수 없는 경우 예외 발생")
    void submit_groupChallengeNotFound() {
        // given
        given(groupChallengeRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> submitService.submit(1L, 1L, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("참가 기록이 없는 경우 예외 발생")
    void submit_recordNotFound() {
        // given
        given(groupChallengeRepository.findById(anyLong())).willReturn(Optional.of(challenge));
        given(recordRepository.findByGroupChallengeIdAndMemberIdAndDeletedAtIsNull(anyLong(), anyLong()))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> submitService.submit(1L, 1L, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_RECORD_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("이미 당일 인증 제출한 경우 예외 발생")
    void submit_alreadySubmittedToday() {
        // given
        GroupChallengeVerification existing = GroupChallengeVerificationFixture.of(participantRecord);
        ReflectionTestUtils.setField(existing, "createdAt", LocalDateTime.now());

        given(groupChallengeRepository.findById(anyLong())).willReturn(Optional.of(challenge));
        given(recordRepository.findByGroupChallengeIdAndMemberIdAndDeletedAtIsNull(anyLong(), anyLong()))
                .willReturn(Optional.of(participantRecord));
        given(verificationRepository
                .findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdOrderByCreatedAtDesc(
                        anyLong(), anyLong()))
                .willReturn(Optional.of(existing));

        // Redis 연산은 예외 발생 전에 도달하지 않으므로 stub 생략

        // when & then
        assertThatThrownBy(() -> submitService.submit(1L, 1L, requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(VerificationErrorCode.ALREADY_SUBMITTED.getMessage());
    }
}
