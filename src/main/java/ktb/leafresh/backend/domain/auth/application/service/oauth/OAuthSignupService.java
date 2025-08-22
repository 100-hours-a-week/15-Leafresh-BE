package ktb.leafresh.backend.domain.auth.application.service.oauth;

import ktb.leafresh.backend.domain.auth.domain.entity.OAuth;
import ktb.leafresh.backend.domain.auth.domain.entity.RefreshToken;
import ktb.leafresh.backend.domain.auth.presentation.dto.request.OAuthSignupRequestDto;
import ktb.leafresh.backend.domain.auth.presentation.dto.response.OAuthSignupResponseDto;
import ktb.leafresh.backend.domain.auth.presentation.dto.result.OAuthSignupResult;
import ktb.leafresh.backend.domain.member.application.service.MemberNicknameCheckService;
import ktb.leafresh.backend.domain.member.application.service.RewardGrantService;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.domain.entity.enums.LoginType;
import ktb.leafresh.backend.domain.member.domain.entity.enums.Role;
import ktb.leafresh.backend.domain.member.domain.entity.enums.TreeLevelName;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.RefreshTokenRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.TreeLevelRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.global.security.TokenProvider;
import ktb.leafresh.backend.global.validator.NicknameValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthSignupService {

  private final MemberNicknameCheckService nicknameCheckService;
  private final RewardGrantService rewardGrantService;
  private final MemberRepository memberRepository;
  private final TreeLevelRepository treeLevelRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final TokenProvider tokenProvider;

  @Transactional
  public OAuthSignupResult signup(OAuthSignupRequestDto request) {
    log.info(
        "회원가입 요청 수신 - email={}, nickname={}, provider={}, providerId={}",
        request.email(),
        request.nickname(),
        request.provider(),
        request.provider().id());

    validateNickname(request.nickname());

    TreeLevel treeLevel = getDefaultTreeLevel();

    Member member = createMember(request, treeLevel);
    memberRepository.save(member);
    log.info("회원 정보 저장 완료 - memberId={}", member.getId());

    rewardGrantService.grantSignupReward(member);

    OAuth oauth =
        OAuth.builder()
            .member(member)
            .provider(request.provider().name())
            .providerId(request.provider().id())
            .build();
    member.getAuths().add(oauth);
    log.info("OAuth 정보 저장 완료 - providerId={}", request.provider().id());

    var tokenDto = tokenProvider.generateTokenDto(member.getId());
    log.info(
        "토큰 발급 완료 - accessTokenLength={}, refreshTokenLength={}",
        tokenDto.getAccessToken() != null ? tokenDto.getAccessToken().length() : 0,
        tokenDto.getRefreshToken() != null ? tokenDto.getRefreshToken().length() : 0);

    saveRefreshToken(member.getId(), tokenDto.getRefreshToken());

    return new OAuthSignupResult(
        new OAuthSignupResponseDto(member.getNickname(), member.getImageUrl()), tokenDto);
  }

  private void validateNickname(String nickname) {
    NicknameValidator.validate(nickname);
    if (nicknameCheckService.isDuplicated(nickname)) {
      throw new CustomException(MemberErrorCode.ALREADY_EXISTS);
    }
  }

  private TreeLevel getDefaultTreeLevel() {
    return treeLevelRepository
        .findByName(TreeLevelName.SPROUT)
        .orElseThrow(
            () -> {
              log.error("기본 TreeLevel 조회 실패 - name={}", TreeLevelName.SPROUT);
              return new CustomException(MemberErrorCode.TREE_LEVEL_NOT_FOUND);
            });
  }

  private Member createMember(OAuthSignupRequestDto request, TreeLevel treeLevel) {
    return Member.builder()
        .email(request.email())
        .loginType(LoginType.SOCIAL)
        .nickname(request.nickname())
        .imageUrl(request.imageUrl())
        .role(Role.USER)
        .activated(true)
        .totalLeafPoints(0)
        .currentLeafPoints(0)
        .treeLevel(treeLevel)
        .build();
  }

  private void saveRefreshToken(Long memberId, String refreshTokenValue) {
    RefreshToken refreshToken =
        RefreshToken.builder().rtKey(String.valueOf(memberId)).rtValue(refreshTokenValue).build();
    refreshTokenRepository.save(refreshToken);
    log.info("RefreshToken 저장 완료 - key={}", refreshToken.getRtKey());
  }
}
