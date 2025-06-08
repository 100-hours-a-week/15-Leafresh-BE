package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.presentation.dto.response.VerificationCountResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.*;

class VerificationCountReadServiceTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOps;
    private VerificationCountReadService service;

    private static final String KEY = "leafresh:totalVerifications:count";

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        service = new VerificationCountReadService(redisTemplate);
    }

    @Test
    @DisplayName("Redis 캐시에 값이 존재하면 해당 값을 반환한다")
    void getTotalVerificationCount_success_cacheHit() {
        // given
        when(valueOps.get(KEY)).thenReturn("42");

        // when
        VerificationCountResponseDto result = service.getTotalVerificationCount();

        // then
        assertThat(result.count()).isEqualTo(42);
    }

    @Test
    @DisplayName("Redis 캐시에 값이 없으면 0을 반환한다")
    void getTotalVerificationCount_success_cacheMiss() {
        // given
        when(valueOps.get(KEY)).thenReturn(null);

        // when
        VerificationCountResponseDto result = service.getTotalVerificationCount();

        // then
        assertThat(result.count()).isEqualTo(0);
    }

    @Test
    @DisplayName("Redis 값이 숫자가 아니면 예외를 던진다")
    void getTotalVerificationCount_fail_invalidFormat() {
        // given
        when(valueOps.get(KEY)).thenReturn("not-a-number");

        // when
        CustomException exception = catchThrowableOfType(
                () -> service.getTotalVerificationCount(),
                CustomException.class
        );

        // then
        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(VerificationErrorCode.VERIFICATION_COUNT_QUERY_FAILED);
    }

    @Test
    @DisplayName("Redis 조회 중 예외가 발생하면 예외를 던진다")
    void getTotalVerificationCount_fail_redisError() {
        // given
        when(valueOps.get(KEY)).thenThrow(new RuntimeException("Redis error"));

        // when
        CustomException exception = catchThrowableOfType(
                () -> service.getTotalVerificationCount(),
                CustomException.class
        );

        // then
        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(VerificationErrorCode.VERIFICATION_COUNT_QUERY_FAILED);
    }
}
