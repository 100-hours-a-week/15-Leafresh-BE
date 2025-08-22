package ktb.leafresh.backend.domain.member.domain.entity.enums;

public enum BadgeType {
  GROUP, // 카테고리별 뱃지 (단체 챌린지 중심)
  PERSONAL, // 개인 챌린지 연속 달성 뱃지
  TOTAL, // 누적 챌린지 인증 수 뱃지 (단체+개인 통합)
  SPECIAL, // 스페셜 뱃지 (이벤트성 or 테마 완주형)
  EVENT // 이벤트 챌린지별 뱃지 설계 (인증 3회 이상 시 자동 지급)
}
