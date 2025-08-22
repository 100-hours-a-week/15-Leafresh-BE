package ktb.leafresh.backend.domain.member.infrastructure.repository;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

  boolean existsByNickname(String nickname);

  Optional<Member> findByEmail(String email);

  boolean existsByNicknameAndIdNot(String nickname, Long id);

  Optional<Member> findByIdAndDeletedAtIsNull(Long id);
}
