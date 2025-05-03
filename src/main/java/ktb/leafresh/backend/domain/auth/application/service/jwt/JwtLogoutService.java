package ktb.leafresh.backend.domain.auth.application.service.jwt;

import ktb.leafresh.backend.domain.auth.domain.entity.RefreshToken;
import ktb.leafresh.backend.domain.member.infrastructure.repository.RefreshTokenRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ErrorCode;
import ktb.leafresh.backend.global.security.JwtProvider;
import ktb.leafresh.backend.global.security.TokenBlacklistService;
import ktb.leafresh.backend.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JwtLogoutService {

    private final TokenProvider tokenProvider;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        validateRefreshToken(refreshToken);

        String memberId = tokenProvider.getAuthentication(accessToken).getName();

        deleteRefreshToken(memberId);
        blacklistAccessToken(accessToken);
    }

    private void validateRefreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN, "유효하지 않은 RefreshToken입니다.");
        }
    }

    private void deleteRefreshToken(String memberId) {
        RefreshToken refreshToken = refreshTokenRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND, "이미 로그아웃된 사용자입니다."));
        refreshTokenRepository.delete(refreshToken);
    }

    private void blacklistAccessToken(String accessToken) {
        long now = System.currentTimeMillis();
        long exp = jwtProvider.getExpiration(accessToken);
        long remainingTime = exp - now;
        if (remainingTime > 0) {
            tokenBlacklistService.blacklistAccessToken(accessToken, remainingTime);
        }
    }
}
