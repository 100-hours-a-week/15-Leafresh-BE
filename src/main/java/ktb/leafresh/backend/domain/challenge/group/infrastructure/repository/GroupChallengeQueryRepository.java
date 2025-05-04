package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;

import java.util.List;

public interface GroupChallengeQueryRepository {
    List<GroupChallenge> findByCategoryWithSearch(Long categoryId, String input, Long cursorId, int size);
}
