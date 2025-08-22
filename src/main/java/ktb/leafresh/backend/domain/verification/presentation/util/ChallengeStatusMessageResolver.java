package ktb.leafresh.backend.domain.verification.presentation.util;

import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;

public class ChallengeStatusMessageResolver {
  public static String resolveMessage(ChallengeStatus status) {
    return switch (status) {
      case NOT_PARTICIPATED -> "아직 챌린지에 참여하지 않았습니다.";
      case NOT_SUBMITTED -> "아직 인증을 제출하지 않았습니다.";
      case PENDING_APPROVAL -> "아직 인증 결과가 도착하지 않았습니다.";
      case SUCCESS -> "인증에 성공했습니다.";
      case FAILURE -> "인증에 실패했습니다.";
    };
  }
}
