package ktb.leafresh.backend.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 JSON 형식입니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    IMAGE_ORDER_INDEX_MISMATCH(HttpStatus.BAD_REQUEST, "이미지 개수와 orderIndex 개수가 맞지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "찾을 수 없습니다."),
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth 공급자입니다."),
    ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    NICKNAME_REQUIRED(HttpStatus.BAD_REQUEST, "닉네임은 필수입니다."),
    NICKNAME_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "닉네임 형식이 올바르지 않습니다. (최소 1자, 최대 20자, 특수문자 제외)"),
    NICKNAME_CHECK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류로 인해 닉네임 중복 검사에 실패했습니다."),
    TREE_LEVEL_NOT_FOUND(HttpStatus.NOT_FOUND, "기본 TreeLevel이 존재하지 않습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "저장된 리프레시 토큰이 없습니다."),
    CHALLENGE_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 챌린지 카테고리입니다."),
    GROUP_CHALLENGE_NOT_FOUND(HttpStatus.NOT_FOUND, "단체 챌린지를 찾을 수 없습니다."),
    CHALLENGE_DURATION_TOO_SHORT(HttpStatus.BAD_REQUEST, "챌린지 기간은 최소 1일 이상이어야 합니다."),
    VERIFICATION_DURATION_TOO_SHORT(HttpStatus.BAD_REQUEST, "인증 가능 시간은 최소 10분 이상이어야 합니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "시작일은 종료일보다 이전이어야 합니다."),
    INVALID_VERIFICATION_TIME(HttpStatus.BAD_REQUEST, "인증 시작 시간은 종료 시간보다 이전이어야 합니다."),
    CHALLENGE_CREATION_REJECTED_BY_AI(HttpStatus.UNPROCESSABLE_ENTITY, "AI 판단 결과 챌린지 생성이 거부되었습니다."),
    CHALLENGE_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "이미 삭제된 단체 챌린지입니다."),
    CHALLENGE_HAS_PARTICIPANTS(HttpStatus.BAD_REQUEST, "해당 챌린지에 참여자가 있어 삭제할 수 없습니다."),
    EXCEEDS_DAILY_PERSONAL_CHALLENGE_LIMIT(HttpStatus.BAD_REQUEST, "요일별 챌린지는 최대 3개까지만 등록할 수 있습니다.");

    private final HttpStatus status;
    private final String message;
}
