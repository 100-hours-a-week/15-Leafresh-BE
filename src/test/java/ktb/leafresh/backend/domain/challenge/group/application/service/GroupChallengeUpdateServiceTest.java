package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.application.service.updater.GroupChallengeCategoryUpdater;
import ktb.leafresh.backend.domain.challenge.group.application.service.updater.GroupChallengeExampleImageUpdater;
import ktb.leafresh.backend.domain.challenge.group.application.service.updater.GroupChallengeUpdater;
import ktb.leafresh.backend.domain.challenge.group.application.validator.GroupChallengeDomainValidator;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeParticipantRecordRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeUpdateRequestDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeUpdateRequestDto.*;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupChallengeUpdateService 테스트")
class GroupChallengeUpdateServiceTest {

    @Mock
    private GroupChallengeUpdater challengeUpdater;

    @Mock
    private GroupChallengeExampleImageUpdater imageUpdater;

    @Mock
    private GroupChallengeCategoryUpdater categoryUpdater;

    @Mock
    private GroupChallengeDomainValidator domainValidator;

    @Mock
    private GroupChallengeParticipantRecordRepository participantRecordRepository;

    @InjectMocks private GroupChallengeUpdateService updateService;

    @Test
    @DisplayName("참여자가 존재하면 수정 불가 예외를 던진다.")
    void update_withParticipants_throwsException() {
        // given
        given(participantRecordRepository.existsByGroupChallengeIdAndDeletedAtIsNull(1L)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> updateService.update(1L, 1L, mock(GroupChallengeUpdateRequestDto.class)))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.CHALLENGE_HAS_PARTICIPANTS_UPDATE_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("유효성 검사 및 업데이트 로직이 정상적으로 호출된다.")
    void update_withoutParticipants_success() {
        // given
        var member = MemberFixture.of();
        var category = GroupChallengeCategoryFixture.defaultCategory();
        var challenge = GroupChallengeFixture.of(member, category);

        var dto = createValidRequest();

        given(participantRecordRepository.existsByGroupChallengeIdAndDeletedAtIsNull(1L)).willReturn(false);
        willDoNothing().given(domainValidator).validate(dto);
        given(challengeUpdater.updateChallengeInfo(1L, 1L, dto)).willReturn(challenge);
        willDoNothing().given(categoryUpdater).updateCategory(challenge, dto.category());
        willDoNothing().given(imageUpdater).updateImages(challenge, dto.exampleImages());

        // when
        updateService.update(1L, 1L, dto);

        // then
        then(domainValidator).should().validate(dto);
        then(challengeUpdater).should().updateChallengeInfo(1L, 1L, dto);
        then(categoryUpdater).should().updateCategory(challenge, dto.category());
        then(imageUpdater).should().updateImages(challenge, dto.exampleImages());
    }

    @Test
    @DisplayName("SecurityException 발생 시 이미지 권한 에러로 변환된다.")
    void update_throwsSecurityException_wrappedAsCustomException() {
        // given
        var dto = createValidRequest();
        given(participantRecordRepository.existsByGroupChallengeIdAndDeletedAtIsNull(1L)).willReturn(false);
        willThrow(SecurityException.class).given(domainValidator).validate(dto);

        // when & then
        assertThatThrownBy(() -> updateService.update(1L, 1L, dto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.CHALLENGE_UPDATE_IMAGE_PERMISSION_DENIED.getMessage());
    }

    @Test
    @DisplayName("알 수 없는 예외가 발생하면 서버 오류로 처리된다.")
    void update_throwsUnknownException_wrappedAsCustomException() {
        // given
        var dto = createValidRequest();
        given(participantRecordRepository.existsByGroupChallengeIdAndDeletedAtIsNull(1L)).willReturn(false);
        willThrow(RuntimeException.class).given(domainValidator).validate(dto);

        // when & then
        assertThatThrownBy(() -> updateService.update(1L, 1L, dto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_UPDATE_FAILED.getMessage());
    }

    private GroupChallengeUpdateRequestDto createValidRequest() {
        return new GroupChallengeUpdateRequestDto(
                "새 제목",
                "새 설명",
                "ZERO_WASTE",
                100,
                "https://dummy.image/thumbnail.png",
                OffsetDateTime.parse("2024-01-01T00:00:00Z"),
                OffsetDateTime.parse("2024-01-07T23:59:00Z"),
                LocalTime.of(6, 0),
                LocalTime.of(22, 0),
                new ExampleImages(
                        List.of(new ExampleImages.KeepImage(1L, 1)),
                        List.of(new ExampleImages.NewImage("https://dummy.image/new.png", ExampleImageType.SUCCESS, "예시", 2)),
                        List.of(3L)
                )
        );
    }
}
