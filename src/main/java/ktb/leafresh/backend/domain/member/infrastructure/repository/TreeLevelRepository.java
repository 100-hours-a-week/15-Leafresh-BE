package ktb.leafresh.backend.domain.member.infrastructure.repository;

import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.domain.entity.enums.TreeLevelName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TreeLevelRepository extends JpaRepository<TreeLevel, Long> {

    Optional<TreeLevel> findByName(TreeLevelName name);
}
