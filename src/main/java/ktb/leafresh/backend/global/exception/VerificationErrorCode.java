package ktb.leafresh.backend.global.exception;

import org.springframework.http.HttpStatus;

public enum VerificationErrorCode implements BaseErrorCode {

    ALREADY_SUBMITTED(HttpStatus.BAD_REQUEST, "오늘은 이미 인증을 완료했습니다."),
    ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "이미 인증이 완료되었습니다."),
    NOT_IN_VERIFICATION_PERIOD(HttpStatus.BAD_REQUEST, "인증 가능 기간이 아닙니다."),
    INVALID_VERIFICATION_TIME(HttpStatus.BAD_REQUEST, "현재 시간에는 인증할 수 없습니다."),
    VERIFICATION_DURATION_TOO_SHORT(HttpStatus.BAD_REQUEST, "인증 가능 시간은 최소 10분 이상이어야 합니다."),
    CONTENT_REQUIRED(HttpStatus.BAD_REQUEST, "인증 내용을 입력해주세요."),
    IMAGE_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, "인증 이미지가 올바르지 않습니다."),
    VERIFICATION_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 챌린지에 참여한 사용자만 인증할 수 있습니다."),
    VERIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 날짜에 인증 내역이 존재하지 않습니다."),
    SUBMISSION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류로 인해 인증 제출에 실패했습니다. 잠시 후 다시 시도해주세요."),
    AI_VERIFICATION_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "이미지에서 인증 조건을 인식할 수 없습니다. 다시 촬영해주세요."),
    AI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AI 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    IMAGE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "업로드한 이미지의 용량이 너무 큽니다."),
    MISSING_IMAGE(HttpStatus.BAD_REQUEST, "이미지는 필수 항목입니다."),
    MISSING_DATE(HttpStatus.BAD_REQUEST, "date는 필수 항목입니다."),
    MISSING_CHALLENGE_ID(HttpStatus.BAD_REQUEST, "challengeId는 필수 항목입니다."),
    MISSING_MEMBER_ID(HttpStatus.BAD_REQUEST, "memberId는 필수 항목입니다."),
    MISSING_CHALLENGE_NAME(HttpStatus.BAD_REQUEST, "challengeName은 필수 항목입니다."),
    MISSING_RESULT(HttpStatus.BAD_REQUEST, "result는 필수 항목입니다."),
    RESULT_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류로 인증 결과 저장 실패. 잠시 후 다시 시도해주세요."),
    CHALLENGE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 챌린지입니다."),
    CHALLENGE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 챌린지에 접근할 수 없습니다."),
    RESULT_QUERY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류로 인해 인증 결과를 조회하지 못했습니다."),
    AI_RESPONSE_FAILED(HttpStatus.BAD_REQUEST, "AI 응답 결과가 검열 실패로 판단되었습니다."),
    AI_RESPONSE_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 응답 파싱에 실패했습니다."),
    AI_REQUEST_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "AI 서버 응답이 시간 초과되었습니다."),
    AI_CONNECTION_FAILED(HttpStatus.BAD_GATEWAY, "AI 서버에 연결할 수 없습니다."),
    VERIFICATION_LIST_QUERY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류로 인해 인증 내역을 조회하지 못했습니다."),
    VERIFICATION_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 인증입니다."),
    VERIFICATION_DETAIL_QUERY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류로 인해 인증 상세 정보를 조회하지 못했습니다."),
    COMMENT_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류로 인해 댓글 작성에 실패했습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    CANNOT_REPLY_TO_DELETED_COMMENT(HttpStatus.BAD_REQUEST, "삭제된 댓글에는 대댓글을 작성할 수 없습니다."),
    COMMENT_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류로 인해 댓글 수정에 실패했습니다."),
    CANNOT_EDIT_DELETED_COMMENT(HttpStatus.BAD_REQUEST, "삭제된 댓글은 수정할 수 없습니다."),
    VERIFICATION_COUNT_QUERY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류로 인해 누적 사용자 인증 수 조회에 실패했습니다."),
    VERIFICATION_SERIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 인증 요청 직렬화에 실패했습니다."),
    VERIFICATION_PUBLISH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI 인증 요청 발행에 실패했습니다.");

    private final HttpStatus status;
    private final String message;

    VerificationErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}
