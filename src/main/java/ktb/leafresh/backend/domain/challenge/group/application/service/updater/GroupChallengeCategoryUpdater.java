package ktb.leafresh.backend.domain.challenge.group.application.service.updater;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupChallengeCategoryUpdater {

    private final GroupChallengeCategoryRepository repository;

    public void updateCategory(GroupChallenge challenge, String categoryName) {
        GroupChallengeCategory newCategory = repository.findByName(categoryName)
                .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_CATEGORY_NOT_FOUND));
        challenge.changeCategory(newCategory);
    }
}
