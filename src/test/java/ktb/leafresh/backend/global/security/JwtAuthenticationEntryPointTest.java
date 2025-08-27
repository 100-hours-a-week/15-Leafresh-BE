package ktb.leafresh.backend.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationEntryPoint 테스트")
class JwtAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    @InjectMocks
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("인증 실패 시 401 상태 코드 반환")
    void commence_SendsUnauthorizedError() throws IOException {
        // when
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // then
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("다른 AuthenticationException으로 401 상태 코드 반환")
    void commence_WithDifferentException_SendsUnauthorizedError() throws IOException {
        // given
        AuthenticationException differentException = new AuthenticationException("Different message") {};

        // when
        jwtAuthenticationEntryPoint.commence(request, response, differentException);

        // then
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("null 예외로도 401 상태 코드 반환")
    void commence_WithNullException_SendsUnauthorizedError() throws IOException {
        // when
        jwtAuthenticationEntryPoint.commence(request, response, null);

        // then
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
