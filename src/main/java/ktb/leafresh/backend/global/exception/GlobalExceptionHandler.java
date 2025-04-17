package ktb.leafresh.backend.global.exception;

import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 커스텀 예외 처리 (비즈니스 로직 관련)
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<String>> handleCustomException(CustomException ex) {
        BaseErrorCode code = ex.getErrorCode();
        log.error("CustomException 처리 - code={}, message={}", code, ex.getMessage());
        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResponse.error(code.getStatus(), ex.getMessage()));
    }

    /**
     * 유효성 검사 실패 예외 처리 (@Valid DTO 검증 실패)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getAllErrors()
                .get(0)
                .getDefaultMessage(); // 첫 번째 에러 메시지만 반환
        log.warn("DTO 유효성 검사 실패 - message={}", errorMessage);
        return ResponseEntity
                .status(GlobalErrorCode.INVALID_REQUEST.getStatus())
                .body(ApiResponse.error(GlobalErrorCode.INVALID_REQUEST.getStatus(), errorMessage));
    }

    /**
     * 인증 예외 처리 (로그인 필요할 때)
     */
    @ExceptionHandler({ AuthenticationException.class, AuthenticationCredentialsNotFoundException.class })
    public ResponseEntity<ApiResponse<String>> handleAuthenticationException(Exception ex) {
        log.warn("인증 실패 - message={}", ex.getMessage());
        return ResponseEntity
                .status(GlobalErrorCode.INVALID_TOKEN.getStatus())
                .body(ApiResponse.error(GlobalErrorCode.INVALID_TOKEN.getStatus(), GlobalErrorCode.INVALID_TOKEN.getMessage()));
    }

    /**
     * 권한 예외 처리 (권한 부족, 403 Forbidden)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<String>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("접근 권한 부족 - message={}", ex.getMessage());
        return ResponseEntity
                .status(GlobalErrorCode.ACCESS_DENIED.getStatus())
                .body(ApiResponse.error(GlobalErrorCode.ACCESS_DENIED.getStatus(), GlobalErrorCode.ACCESS_DENIED.getMessage()));
    }

    /**
     * JSON 파싱 실패 예외 처리
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleParseException(HttpMessageNotReadableException ex) {
        log.error("요청 본문 파싱 실패 - message={}", ex.getMessage(), ex);
        return ResponseEntity
                .status(GlobalErrorCode.INVALID_JSON_FORMAT.getStatus())
                .body(ApiResponse.error(GlobalErrorCode.INVALID_JSON_FORMAT.getStatus(), GlobalErrorCode.INVALID_JSON_FORMAT.getMessage()));
    }

    /**
     * 정적 리소스가 존재하지 않을 때 (ex. favicon.ico 요청 실패)
     * - 에러 로그로 남기지 않고 404만 응답
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFoundException(NoResourceFoundException ex) {
        // 로그를 굳이 남길 필요 없다면 생략 가능
        log.debug("정적 리소스를 찾을 수 없습니다.");
        return ResponseEntity.notFound().build();
    }

    /**
     * 기타 예상치 못한 예외 처리 (서버 내부 오류)
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiResponse<String>> handleException(Exception ex) {
        log.error("예상치 못한 서버 내부 오류 - message={}", ex.getMessage(), ex);
        return ResponseEntity
                .status(GlobalErrorCode.NOT_FOUND.getStatus())
                .body(ApiResponse.error(GlobalErrorCode.NOT_FOUND.getStatus(), GlobalErrorCode.NOT_FOUND.getMessage()));
    }
}
