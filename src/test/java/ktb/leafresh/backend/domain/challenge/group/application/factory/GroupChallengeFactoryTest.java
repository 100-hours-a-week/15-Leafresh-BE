package ktb.leafresh.backend.domain.challenge.group.application.factory;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeCreateRequestDto;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static ktb.leafresh.backend.global.common.entity.enums.ExampleImageType.SUCCESS;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupChallengeFactory 테스트")
class GroupChallengeFactoryTest {

  @Mock private GroupChallengeCategoryRepository categoryRepository;

  @InjectMocks private GroupChallengeFactory groupChallengeFactory;

  @Test
  @DisplayName("단체 챌린지를 정상적으로 생성할 수 있다")
  void createGroupChallenge_withValidInput_returnsGroupChallenge() {
    // given
    GroupChallengeCategory category = GroupChallengeCategoryFixture.of("ZERO_WASTE");
    Member member = MemberFixture.of();

    GroupChallengeCreateRequestDto.ExampleImageRequestDto exampleImage =
        new GroupChallengeCreateRequestDto.ExampleImageRequestDto(
            "https://dummy.image/example.png", SUCCESS, "성공 인증 예시입니다.", 1);

    GroupChallengeCreateRequestDto dto =
        new GroupChallengeCreateRequestDto(
            "제로웨이스트 챌린지",
            "플라스틱 줄이기 실천",
            category.getName(),
            50,
            "https://dummy.image/thumbnail.png",
            OffsetDateTime.parse("2024-01-01T00:00:00+09:00"),
            OffsetDateTime.parse("2024-01-07T23:59:00+09:00"),
            LocalTime.of(6, 0),
            LocalTime.of(22, 0),
            List.of(exampleImage));

    given(categoryRepository.findByName(dto.category())).willReturn(Optional.of(category));

    // when
    GroupChallenge result = groupChallengeFactory.create(dto, member);

    // then
    assertThat(result.getMember()).isEqualTo(member);
    assertThat(result.getCategory()).isEqualTo(category);
    assertThat(result.getTitle()).isEqualTo(dto.title());
    assertThat(result.getDescription()).isEqualTo(dto.description());
    assertThat(result.getImageUrl()).isEqualTo(dto.thumbnailImageUrl());
    assertThat(result.getStartDate()).isEqualTo(dto.startDate().toLocalDateTime());
    assertThat(result.getEndDate()).isEqualTo(dto.endDate().toLocalDateTime());
    assertThat(result.getVerificationStartTime()).isEqualTo(dto.verificationStartTime());
    assertThat(result.getVerificationEndTime()).isEqualTo(dto.verificationEndTime());
    assertThat(result.getMaxParticipantCount()).isEqualTo(dto.maxParticipantCount());
    assertThat(result.getCurrentParticipantCount()).isZero(); // 초기값 0
    assertThat(result.getLeafReward()).isEqualTo(200); // 고정값
  }

  @Test
  @DisplayName("존재하지 않는 카테고리로 챌린지 생성 시 예외가 발생한다")
  void createGroupChallenge_withInvalidCategory_throwsException() {
    // given
    Member member = MemberFixture.of();

    GroupChallengeCreateRequestDto dto =
        new GroupChallengeCreateRequestDto(
            "제로웨이스트 챌린지",
            "플라스틱 줄이기 실천",
            "NON_EXISTING_CATEGORY",
            50,
            "https://dummy.image/thumbnail.png",
            OffsetDateTime.now(),
            OffsetDateTime.now().plusDays(7),
            LocalTime.of(6, 0),
            LocalTime.of(22, 0),
            List.of(
                new GroupChallengeCreateRequestDto.ExampleImageRequestDto(
                    "https://dummy.image/example.png", SUCCESS, "설명", 1)));

    given(categoryRepository.findByName(dto.category())).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> groupChallengeFactory.create(dto, member))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ChallengeErrorCode.CHALLENGE_CATEGORY_NOT_FOUND.getMessage());
  }
}
