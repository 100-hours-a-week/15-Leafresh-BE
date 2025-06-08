package ktb.leafresh.backend.domain.verification.infrastructure.repository;

import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;

import java.util.List;

public interface GroupChallengeVerificationFeedQueryRepository {
    List<GroupChallengeVerification> findAllByFilter(String category, Long cursorId, String cursorTimestamp, int size);
}
