package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface GroupChallengeCategoryRepository
    extends JpaRepository<GroupChallengeCategory, Long> {

  Optional<GroupChallengeCategory> findByName(String name);

  List<GroupChallengeCategory> findAllByActivatedIsTrueOrderBySequenceNumberAsc();
}
