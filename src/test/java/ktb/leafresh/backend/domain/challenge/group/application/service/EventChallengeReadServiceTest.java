package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.EventChallengeResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture.of;
import static ktb.leafresh.backend.support.fixture.GroupChallengeFixture.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventChallengeReadService 테스트")
class EventChallengeReadServiceTest {

    @Mock
    private GroupChallengeRepository groupChallengeRepository;

    @InjectMocks
    private EventChallengeReadService eventChallengeReadService;

    private GroupChallenge challenge;

    @BeforeEach
    void setUp() {
        challenge = of(mock(ktb.leafresh.backend.domain.member.domain.entity.Member.class), of("ETC"), "챌린지 제목", true);
    }

    @Test
    @DisplayName("2주 이내의 이벤트 챌린지 목록을 조회할 수 있다")
    void getEventChallenges_withinTwoWeeks_returnsDtoList() {
        // given
        LocalDateTime now = LocalDateTime.now();
        given(groupChallengeRepository.findEventChallengesWithinRange(any(), any()))
                .willReturn(List.of(challenge));

        // when
        List<EventChallengeResponseDto> result = eventChallengeReadService.getEventChallenges();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(challenge.getId());
        assertThat(result.get(0).title()).isEqualTo(challenge.getTitle());
        assertThat(result.get(0).description()).isEqualTo(challenge.getDescription());
        assertThat(result.get(0).thumbnailUrl()).isEqualTo(challenge.getImageUrl());
    }

    @Test
    @DisplayName("조회 중 예외가 발생하면 CustomException이 발생한다")
    void getEventChallenges_whenExceptionThrown_throwsCustomException() {
        // given
        given(groupChallengeRepository.findEventChallengesWithinRange(any(), any()))
                .willThrow(new RuntimeException("DB 오류"));

        // when & then
        assertThatThrownBy(() -> eventChallengeReadService.getEventChallenges())
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.EVENT_CHALLENGE_READ_FAILED.getMessage());
    }
}
