package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;

import java.util.List;

public interface GroupChallengeCreatedQueryRepository {
  List<GroupChallenge> findCreatedByMember(
      Long memberId, Long cursorId, String cursorTimestamp, int size);
}
