package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeExampleImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupChallengeExampleImageRepository
    extends JpaRepository<GroupChallengeExampleImage, Long> {}
