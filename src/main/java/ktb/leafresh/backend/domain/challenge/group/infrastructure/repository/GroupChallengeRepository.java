package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupChallengeRepository extends JpaRepository<GroupChallenge, Long> {
}
