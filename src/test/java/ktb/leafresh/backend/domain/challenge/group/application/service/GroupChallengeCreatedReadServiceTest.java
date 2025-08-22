package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCreatedQueryRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.CreatedGroupChallengeSummaryResponseDto;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GroupChallengeCreatedReadServiceTest {

  @Mock private GroupChallengeCreatedQueryRepository createdRepository;

  @InjectMocks private GroupChallengeCreatedReadService createdReadService;

  @Test
  @DisplayName("내가 생성한 단체 챌린지를 커서 기반으로 조회할 수 있다")
  void getCreatedChallengesByMember_withValidInput_returnsPaginatedResult() {
    // given
    Long memberId = 1L;
    Long cursorId = null;
    String cursorTimestamp = null;
    int size = 2;

    var member = MemberFixture.of();
    var category = GroupChallengeCategoryFixture.of("환경");
    var challenge1 = GroupChallengeFixture.of(member, category);
    var challenge2 = GroupChallengeFixture.of(member, category);
    var challenge3 = GroupChallengeFixture.of(member, category);

    // 2개만 조회 요청했지만, hasNext 검증을 위해 +1개 반환
    LocalDateTime fixedCreatedAt = LocalDateTime.of(2024, 5, 1, 12, 0);
    ReflectionTestUtils.setField(challenge1, "createdAt", fixedCreatedAt);
    ReflectionTestUtils.setField(challenge2, "createdAt", fixedCreatedAt.plusMinutes(1));
    ReflectionTestUtils.setField(challenge3, "createdAt", fixedCreatedAt.plusMinutes(2));
    given(createdRepository.findCreatedByMember(memberId, cursorId, cursorTimestamp, size + 1))
        .willReturn(List.of(challenge1, challenge2, challenge3));

    // when
    CursorPaginationResult<CreatedGroupChallengeSummaryResponseDto> result =
        createdReadService.getCreatedChallengesByMember(memberId, cursorId, cursorTimestamp, size);

    // then
    assertThat(result.items()).hasSize(size);
    assertThat(result.hasNext()).isTrue(); // 3개 중 2개만 반환되므로 다음 페이지 있음
    assertThat(result.cursorInfo()).isNotNull();

    var first = result.items().get(0);
    assertThat(first.name()).isEqualTo(challenge1.getTitle());
    assertThat(first.description()).isEqualTo(challenge1.getDescription());
    assertThat(first.imageUrl()).isEqualTo(challenge1.getImageUrl());
    assertThat(first.startDate()).isEqualTo(challenge1.getStartDate().toLocalDate().toString());
    assertThat(first.endDate()).isEqualTo(challenge1.getEndDate().toLocalDate().toString());
    assertThat(first.currentParticipantCount()).isEqualTo(challenge1.getCurrentParticipantCount());
    assertThat(first.category()).isEqualTo(challenge1.getCategory().getName());

    // 커서 시간은 UTC로 변환되었는지 검증
    String expectedTime =
        challenge1
            .getCreatedAt()
            .atOffset(java.time.ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    String actualTime = first.createdAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    assertThat(actualTime).isEqualTo(expectedTime);
  }
}
