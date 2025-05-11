package ktb.leafresh.backend.global.exception;

import org.springframework.http.HttpStatus;

public enum ChallengeErrorCode implements BaseErrorCode {

    // 공용
    VERIFICATION_DURATION_TOO_SHORT(HttpStatus.BAD_REQUEST, "인증 가능 시간은 최소 10분 이상이어야 합니다."),
    INVALID_VERIFICATION_TIME(HttpStatus.BAD_REQUEST, "인증 시작 시간은 종료 시간보다 이전이어야 합니다."),
    CHALLENGE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "챌린지 삭제 권한이 없습니다."),

    // 단체
    EVENT_CHALLENGE_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류로 인해 이벤트 챌린지 목록 조회에 실패했습니다."),
    CHALLENGE_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 챌린지 카테고리입니다."),
    GROUP_CHALLENGE_NOT_FOUND(HttpStatus.NOT_FOUND, "단체 챌린지를 찾을 수 없습니다."),
    GROUP_CHALLENGE_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "참여 기록이 존재하지 않습니다."),
    GROUP_CHALLENGE_PARTICIPATION_NOT_FOUND(HttpStatus.NOT_FOUND, "단체 챌린지 참여 정보를 찾을 수 없습니다."),
    CHALLENGE_DURATION_TOO_SHORT(HttpStatus.BAD_REQUEST, "챌린지 기간은 최소 1일 이상이어야 합니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "시작일은 종료일보다 이전이어야 합니다."),
    CHALLENGE_CREATION_REJECTED_BY_AI(HttpStatus.UNPROCESSABLE_ENTITY, "AI 판단 결과 챌린지 생성이 거부되었습니다."),
    CHALLENGE_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 단체 챌린지입니다."),
    CHALLENGE_HAS_PARTICIPANTS(HttpStatus.BAD_REQUEST, "해당 챌린지에 참여자가 있어 삭제할 수 없습니다."),
    CHALLENGE_ALREADY_PARTICIPATED(HttpStatus.BAD_REQUEST, "이미 참여한 챌린지입니다."),
    CHALLENGE_FULL(HttpStatus.FORBIDDEN, "참여 인원이 초과되었습니다."),
    CHALLENGE_ALREADY_DROPPED(HttpStatus.BAD_REQUEST, "이미 취소된 참여 이력입니다."),

    // 개인
    PERSONAL_CHALLENGE_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류로 인해 개인 챌린지 목록 조회에 실패했습니다."),
    EXCEEDS_DAILY_PERSONAL_CHALLENGE_LIMIT(HttpStatus.BAD_REQUEST, "요일별 챌린지는 최대 3개까지만 등록할 수 있습니다."),
    PERSONAL_CHALLENGE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 개인 챌린지를 찾을 수 없습니다."),
    PERSONAL_CHALLENGE_EMPTY(HttpStatus.NOT_FOUND, "현재 등록된 개인 챌린지가 없습니다."),
    PERSONAL_CHALLENGE_DETAIL_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류로 인해 개인 챌린지 상세 조회에 실패했습니다."),
    PERSONAL_CHALLENGE_DETAIL_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "현재 개인 챌린지 상세 정보를 불러올 수 없습니다. 잠시 후 다시 시도해주세요."),
    PERSONAL_CHALLENGE_RULE_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류로 인해 인증 규약을 조회하지 못했습니다."),
    PERSONAL_CHALLENGE_RULE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 챌린지에 참여 중인 사용자가 아닙니다."),
    PERSONAL_CHALLENGE_RULE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 챌린지입니다.");

    private final HttpStatus status;
    private final String message;

    ChallengeErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}
