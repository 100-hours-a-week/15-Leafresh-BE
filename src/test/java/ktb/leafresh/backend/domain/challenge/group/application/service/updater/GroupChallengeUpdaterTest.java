package ktb.leafresh.backend.domain.challenge.group.application.service.updater;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeUpdateRequestDto;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("GroupChallengeUpdater 테스트")
@ExtendWith(MockitoExtension.class)
class GroupChallengeUpdaterTest {

    @Mock
    private GroupChallengeRepository repository;

    @InjectMocks
    private GroupChallengeUpdater updater;

    private Member member;
    private GroupChallenge challenge;

    @BeforeEach
    void setUp() {
        member = MemberFixture.of();
        ReflectionTestUtils.setField(member, "id", 10L);

        GroupChallengeCategory category = GroupChallengeCategoryFixture.defaultCategory();
        challenge = GroupChallengeFixture.of(member, category);
        ReflectionTestUtils.setField(challenge, "id", 1L);
    }

    @Test
    @DisplayName("정상적으로 챌린지를 수정할 수 있다")
    void updateChallengeInfo_withValidInput_success() {
        // given
        GroupChallengeUpdateRequestDto dto = new GroupChallengeUpdateRequestDto(
                "새로운 타이틀",
                "새로운 설명",
                "ZERO_WASTE",
                50,
                "https://image.new-thumbnail.com/image.jpg",
                OffsetDateTime.parse("2025-01-01T00:00:00+09:00"),
                OffsetDateTime.parse("2025-01-10T23:59:00+09:00"),
                LocalTime.of(5, 0),
                LocalTime.of(21, 0),
                null // 이미지 업데이트는 해당 로직에서 제외
        );

        given(repository.findById(1L)).willReturn(Optional.of(challenge));

        // when
        GroupChallenge updated = updater.updateChallengeInfo(10L, 1L, dto);

        // then
        assertThat(updated.getTitle()).isEqualTo(dto.title());
        assertThat(updated.getDescription()).isEqualTo(dto.description());
        assertThat(updated.getImageUrl()).isEqualTo(dto.thumbnailImageUrl());
        assertThat(updated.getMaxParticipantCount()).isEqualTo(dto.maxParticipantCount());
        assertThat(updated.getStartDate().truncatedTo(ChronoUnit.MINUTES))
                .isEqualTo(dto.startDate().toLocalDateTime().truncatedTo(ChronoUnit.MINUTES));
        assertThat(updated.getEndDate().truncatedTo(ChronoUnit.MINUTES))
                .isEqualTo(dto.endDate().toLocalDateTime().truncatedTo(ChronoUnit.MINUTES));
        assertThat(updated.getVerificationStartTime()).isEqualTo(dto.verificationStartTime());
        assertThat(updated.getVerificationEndTime()).isEqualTo(dto.verificationEndTime());
    }

    @Test
    @DisplayName("존재하지 않는 챌린지를 수정하려 하면 예외가 발생한다")
    void updateChallengeInfo_notFound_throwsException() {
        // given
        GroupChallengeUpdateRequestDto dto = mock(GroupChallengeUpdateRequestDto.class);

        given(repository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> updater.updateChallengeInfo(10L, 999L, dto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("작성자가 아닌 사용자가 수정하려 하면 예외가 발생한다")
    void updateChallengeInfo_invalidMember_throwsAccessDenied() {
        // given
        Member other = MemberFixture.of("other@leafresh.com", "다른사람");
        ReflectionTestUtils.setField(other, "id", 999L);

        GroupChallengeUpdateRequestDto dto = mock(GroupChallengeUpdateRequestDto.class);

        given(repository.findById(1L)).willReturn(Optional.of(challenge));

        // when & then
        assertThatThrownBy(() -> updater.updateChallengeInfo(999L, 1L, dto))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(GlobalErrorCode.ACCESS_DENIED.getMessage());
    }
}
