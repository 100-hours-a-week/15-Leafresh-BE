package ktb.leafresh.backend.domain.challenge.group.application.service.updater;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeUpdateRequestDto;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupChallengeUpdater {

    private final GroupChallengeRepository repository;

    public GroupChallenge updateChallengeInfo(Long memberId, Long challengeId, GroupChallengeUpdateRequestDto dto) {
        GroupChallenge challenge = repository.findById(challengeId)
                .orElseThrow(() -> new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_NOT_FOUND));

        if (!challenge.getMember().getId().equals(memberId)) {
            throw new CustomException(GlobalErrorCode.ACCESS_DENIED);
        }

        challenge.updateInfo(
                dto.title(), dto.description(), dto.thumbnailImageUrl(), dto.maxParticipantCount(),
                dto.startDate(), dto.endDate(), dto.verificationStartTime(), dto.verificationEndTime()
        );

        return challenge;
    }
}
