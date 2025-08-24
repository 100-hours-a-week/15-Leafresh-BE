package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeParticipationRecordQueryRepository;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeVerificationQueryRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.query.GroupChallengeParticipationDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeParticipationCountResponseDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeParticipationCountSummaryDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeParticipationListResponseDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeParticipationSummaryDto.AchievementRecordDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupChallengeParticipationReadService 테스트")
class GroupChallengeParticipationReadServiceTest {

  @Mock private GroupChallengeParticipationRecordQueryRepository participationQueryRepository;

  @Mock private GroupChallengeVerificationQueryRepository verificationQueryRepository;

  @InjectMocks private GroupChallengeParticipationReadService participationReadService;

  @Nested
  @DisplayName("getParticipationCounts()는")
  class GetParticipationCounts {

    @Test
    @DisplayName("상태별 참여 챌린지 수를 조회해 반환한다.")
    void returnsParticipationCounts() {
      // given
      Long memberId = 1L;
      GroupChallengeParticipationCountSummaryDto summaryDto =
          new GroupChallengeParticipationCountSummaryDto(1, 2, 3);

      given(participationQueryRepository.countParticipationByStatus(memberId))
          .willReturn(summaryDto);

      // when
      GroupChallengeParticipationCountResponseDto response =
          participationReadService.getParticipationCounts(memberId);

      // then
      assertThat(response).isNotNull();
      assertThat(response.count().notStarted()).isEqualTo(1);
      assertThat(response.count().ongoing()).isEqualTo(2);
      assertThat(response.count().completed()).isEqualTo(3);
    }
  }

  @Nested
  @DisplayName("getParticipatedChallenges()는")
  class GetParticipatedChallenges {

    @Test
    @DisplayName("참여한 챌린지 목록을 커서 기반으로 조회해 반환한다.")
    void returnsParticipatedChallengesWithPagination() {
      // given
      Long memberId = 1L;
      String status = "ONGOING";
      Long cursorId = null;
      String cursorTimestamp = null;
      int size = 2;

      GroupChallengeParticipationDto dto1 =
          new GroupChallengeParticipationDto(
              10L,
              "제로웨이스트",
              "https://dummy.image/challenge1.png",
              LocalDateTime.of(2024, 1, 1, 0, 0),
              LocalDateTime.of(2024, 1, 7, 23, 59),
              3L,
              5L,
              LocalDateTime.of(2024, 1, 1, 0, 0));

      GroupChallengeParticipationDto dto2 =
          new GroupChallengeParticipationDto(
              11L,
              "플로깅",
              "https://dummy.image/challenge2.png",
              LocalDateTime.of(2024, 2, 1, 0, 0),
              LocalDateTime.of(2024, 2, 7, 23, 59),
              2L,
              5L,
              LocalDateTime.of(2024, 2, 1, 0, 0));

      List<GroupChallengeParticipationDto> dtos = List.of(dto1, dto2);
      List<AchievementRecordDto> record1 = List.of(new AchievementRecordDto(1, "SUCCESS"));
      List<AchievementRecordDto> record2 = List.of(new AchievementRecordDto(2, "FAIL"));

      Map<Long, List<AchievementRecordDto>> recordMap =
          Map.of(
              10L, record1,
              11L, record2);

      given(
              participationQueryRepository.findParticipatedByStatus(
                  memberId, status, cursorId, cursorTimestamp, size + 1))
          .willReturn(dtos);

      given(
              verificationQueryRepository.findVerificationsGroupedByChallenge(
                  List.of(10L, 11L), memberId))
          .willReturn(recordMap);

      // when
      GroupChallengeParticipationListResponseDto response =
          participationReadService.getParticipatedChallenges(
              memberId, status, cursorId, cursorTimestamp, size);

      // then
      assertThat(response).isNotNull();
      assertThat(response.challenges()).hasSize(2);
      assertThat(response.hasNext()).isFalse();
      assertThat(response.challenges().get(0).achievement().success()).isEqualTo(3L);
      assertThat(response.challenges().get(1).achievementRecords())
          .containsExactly(new AchievementRecordDto(2, "FAIL"));
    }
  }
}
