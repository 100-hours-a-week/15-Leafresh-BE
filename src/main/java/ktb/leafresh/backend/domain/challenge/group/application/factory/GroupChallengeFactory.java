package ktb.leafresh.backend.domain.challenge.group.application.factory;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeCreateRequestDto;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupChallengeFactory {

    private final GroupChallengeCategoryRepository categoryRepository;

    public GroupChallenge create(GroupChallengeCreateRequestDto dto, Member member) {
        GroupChallengeCategory category = categoryRepository.findByName(dto.category())
                .orElseThrow(() -> new CustomException(ChallengeErrorCode.CHALLENGE_CATEGORY_NOT_FOUND));

        return GroupChallenge.builder()
                .member(member)
                .category(category)
                .title(dto.title())
                .description(dto.description())
                .imageUrl(dto.thumbnailImageUrl())
                .startDate(dto.startDate().atStartOfDay())
                .endDate(dto.endDate().atTime(23, 59, 59))
                .verificationStartTime(dto.verificationStartTime())
                .verificationEndTime(dto.verificationEndTime())
                .maxParticipantCount(dto.maxParticipantCount())
                .currentParticipantCount(0)
                .leafReward(30)
                .build();
    }
}
