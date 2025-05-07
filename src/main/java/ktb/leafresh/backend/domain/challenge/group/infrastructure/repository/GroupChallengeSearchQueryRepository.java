package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;

import java.util.List;

public interface GroupChallengeSearchQueryRepository {
    List<GroupChallenge> findByFilter(String input, String category, Long cursorId, int size);
}
