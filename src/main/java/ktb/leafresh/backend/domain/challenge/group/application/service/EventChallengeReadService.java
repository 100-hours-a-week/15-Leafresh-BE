package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.EventChallengeResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventChallengeReadService {

  private final GroupChallengeRepository groupChallengeRepository;

  public List<EventChallengeResponseDto> getEventChallenges() {
    try {
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime twoWeeksLater = now.plusWeeks(2);
      List<GroupChallenge> challenges =
          groupChallengeRepository.findEventChallengesWithinRange(now, twoWeeksLater);
      return challenges.stream().map(EventChallengeResponseDto::from).collect(Collectors.toList());
    } catch (Exception e) {
      throw new CustomException(ChallengeErrorCode.EVENT_CHALLENGE_READ_FAILED);
    }
  }
}
