package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeParticipantRecordRepository;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.domain.support.validator.VerificationSubmitValidator;
import ktb.leafresh.backend.domain.verification.infrastructure.publisher.GcpAiVerificationPubSubPublisher;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.GroupChallengeVerificationRequestDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import ktb.leafresh.backend.support.fixture.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GroupChallengeVerificationSubmitServiceTest {

    private GroupChallengeRepository groupChallengeRepository;
    private GroupChallengeParticipantRecordRepository recordRepository;
    private GroupChallengeVerificationRepository verificationRepository;
    private VerificationSubmitValidator validator;
    private StringRedisTemplate redisTemplate;
    private GcpAiVerificationPubSubPublisher pubSubPublisher;
    private GroupChallengeVerificationSubmitService service;

    @BeforeEach
    void setUp() {
        groupChallengeRepository = mock(GroupChallengeRepository.class);
        recordRepository = mock(GroupChallengeParticipantRecordRepository.class);
        verificationRepository = mock(GroupChallengeVerificationRepository.class);
        validator = mock(VerificationSubmitValidator.class);
        redisTemplate = mock(StringRedisTemplate.class);
        pubSubPublisher = mock(GcpAiVerificationPubSubPublisher.class);

        service = new GroupChallengeVerificationSubmitService(
                groupChallengeRepository,
                recordRepository,
                verificationRepository,
                validator,
                redisTemplate,
                pubSubPublisher
        );
    }

    @Test
    @DisplayName("단체 챌린지 인증 제출에 성공하면 저장 및 Pub/Sub 발행, Redis 증가가 수행된다")
    void submit_Success() {
        // Given
        Long memberId = 1L;
        Long challengeId = 2L;

        var member = MemberFixture.of();
        var category = GroupChallengeCategoryFixture.of("제로웨이스트");
        var challenge = GroupChallengeFixture.of(member, category);
        var record = GroupChallengeParticipantRecordFixture.of(challenge, member);

        GroupChallengeVerificationRequestDto dto = new GroupChallengeVerificationRequestDto(
                "https://dummy.image/verify.jpg",
                "이건 인증 내용입니다."
        );

        when(groupChallengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(recordRepository.findByGroupChallengeIdAndMemberIdAndDeletedAtIsNull(challengeId, memberId))
                .thenReturn(Optional.of(record));
        when(verificationRepository
                .findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdOrderByCreatedAtDesc(
                        memberId, challengeId))
                .thenReturn(Optional.empty());
        when(verificationRepository.save(any(GroupChallengeVerification.class)))
                .thenAnswer(invocation -> {
                    GroupChallengeVerification saved = invocation.getArgument(0);

                    // 리플렉션으로 id 필드에 직접 값 주입
                    Field idField = GroupChallengeVerification.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(saved, 100L);

                    return saved;
                });

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        // When
        service.submit(memberId, challengeId, dto);

        // Then
        verify(validator).validate(dto.content());
        verify(verificationRepository).save(any(GroupChallengeVerification.class));
        verify(pubSubPublisher).publishAsyncWithRetry(any());
        verify(redisTemplate.opsForValue()).increment("leafresh:totalVerifications:count");
    }

    @Test
    @DisplayName("이미 오늘 인증한 경우 예외를 발생시킨다")
    void submit_AlreadySubmitted_ThrowsException() {
        // Given
        Long memberId = 1L;
        Long challengeId = 2L;

        var member = MemberFixture.of();
        var category = GroupChallengeCategoryFixture.of("제로웨이스트");
        var challenge = GroupChallengeFixture.of(member, category);
        var record = GroupChallengeParticipantRecordFixture.of(challenge, member);
        var verification = GroupChallengeVerificationFixture.of(record);

        GroupChallengeVerificationRequestDto dto = new GroupChallengeVerificationRequestDto(
                "https://dummy.image/verify.jpg",
                "오늘 이미 인증함"
        );

        when(groupChallengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge));
        when(recordRepository.findByGroupChallengeIdAndMemberIdAndDeletedAtIsNull(challengeId, memberId))
                .thenReturn(Optional.of(record));
        when(verificationRepository
                .findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdOrderByCreatedAtDesc(
                        memberId, challengeId))
                .thenReturn(Optional.of(verification)); // 오늘 인증했으므로

        // Expect
        assertThatThrownBy(() -> service.submit(memberId, challengeId, dto))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> {
                    CustomException ce = (CustomException) ex;
                    assertThat(ce.getErrorCode()).isEqualTo(VerificationErrorCode.ALREADY_SUBMITTED);
                });

        verify(verificationRepository, never()).save(any());
        verify(pubSubPublisher, never()).publishAsyncWithRetry(any());
    }
}
