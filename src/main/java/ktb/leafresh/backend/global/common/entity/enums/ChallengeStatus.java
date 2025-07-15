package ktb.leafresh.backend.global.common.entity.enums;

public enum ChallengeStatus {
    NOT_PARTICIPATED,   // 챌린지에 참여하지 않음
    NOT_SUBMITTED,      // 오늘 인증 미제출
    PENDING_APPROVAL,   // 오늘 인증 제출, 검토 대기
    SUCCESS,            // 오늘 인증 성공
    FAILURE             // 오늘 인증 실패
}
