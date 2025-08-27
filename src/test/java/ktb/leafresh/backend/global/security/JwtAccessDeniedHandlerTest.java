package ktb.leafresh.backend.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAccessDeniedHandler 테스트")
class JwtAccessDeniedHandlerTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AccessDeniedException accessDeniedException;

    @InjectMocks
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Test
    @DisplayName("접근 거부 시 403 상태 코드 반환")
    void handle_SendsForbiddenError() throws IOException, ServletException {
        // when
        jwtAccessDeniedHandler.handle(request, response, accessDeniedException);

        // then
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("다른 AccessDeniedException으로 403 상태 코드 반환")
    void handle_WithDifferentException_SendsForbiddenError() throws IOException, ServletException {
        // given
        AccessDeniedException differentException = new AccessDeniedException("Different message");

        // when
        jwtAccessDeniedHandler.handle(request, response, differentException);

        // then
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("null 예외로도 403 상태 코드 반환")
    void handle_WithNullException_SendsForbiddenError() throws IOException, ServletException {
        // when
        jwtAccessDeniedHandler.handle(request, response, null);

        // then
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("빈 메시지 예외로 403 상태 코드 반환")
    void handle_WithEmptyMessageException_SendsForbiddenError() throws IOException, ServletException {
        // given
        AccessDeniedException emptyMessageException = new AccessDeniedException("");

        // when
        jwtAccessDeniedHandler.handle(request, response, emptyMessageException);

        // then
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("null 메시지 예외로 403 상태 코드 반환")
    void handle_WithNullMessageException_SendsForbiddenError() throws IOException, ServletException {
        // given
        AccessDeniedException nullMessageException = new AccessDeniedException(null);

        // when
        jwtAccessDeniedHandler.handle(request, response, nullMessageException);

        // then
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
    }
}
