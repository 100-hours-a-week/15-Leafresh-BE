package ktb.leafresh.backend.domain.member.infrastructure.repository;

import ktb.leafresh.backend.domain.auth.domain.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {}
