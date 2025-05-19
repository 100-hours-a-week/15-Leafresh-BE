package ktb.leafresh.backend.global.exception;

import org.springframework.http.HttpStatus;

public enum MemberErrorCode implements BaseErrorCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth 공급자입니다."),
    ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    NICKNAME_REQUIRED(HttpStatus.BAD_REQUEST, "닉네임은 필수입니다."),
    NICKNAME_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "닉네임 형식이 올바르지 않습니다. (최소 1자, 최대 20자, 특수문자 제외)"),
    NICKNAME_CHECK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류로 인해 닉네임 중복 검사에 실패했습니다."),
    TREE_LEVEL_NOT_FOUND(HttpStatus.NOT_FOUND, "기본 TreeLevel이 존재하지 않습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "저장된 리프레시 토큰이 없습니다."),
    INVALID_AUTHORIZATION_CODE(HttpStatus.BAD_REQUEST, "유효하지 않은 인가 코드입니다."),
    KAKAO_LOGIN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류로 인해 카카오 로그인에 실패했습니다."),
    KAKAO_TOKEN_ISSUE_FAILED(HttpStatus.BAD_GATEWAY, "카카오 accessToken 발급 중 오류가 발생했습니다."),
    SIGNUP_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류로 인해 회원가입에 실패했습니다."),
    INVALID_LOGOUT_REQUEST(HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다. accessToken이 존재하지 않습니다."),
    LOGOUT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류로 인해 로그아웃에 실패했습니다."),
    KAKAO_LOGOUT_FAILED(HttpStatus.BAD_GATEWAY, "카카오 로그아웃 처리 중 오류가 발생했습니다."),
    NICKNAME_UPDATE_FORBIDDEN(HttpStatus.FORBIDDEN, "닉네임 수정 권한이 없습니다."),
    NICKNAME_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류로 인해 닉네임 변경에 실패했습니다."),
    MEMBER_INFO_QUERY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류로 인해 회원 정보를 조회하지 못했습니다.");

    private final HttpStatus status;
    private final String message;

    MemberErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}
