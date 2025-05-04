package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.EventChallengeResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventChallengeReadService {

    private final GroupChallengeRepository groupChallengeRepository;

    public List<EventChallengeResponseDto> getEventChallenges() {
        LocalDateTime now = LocalDateTime.now();
        List<GroupChallenge> challenges = groupChallengeRepository.findOngoingEventChallenges(now);
        return challenges.stream()
                .map(EventChallengeResponseDto::from)
                .collect(toList());
    }
}
