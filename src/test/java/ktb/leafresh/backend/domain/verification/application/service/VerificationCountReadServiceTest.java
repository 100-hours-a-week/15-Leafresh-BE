package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.presentation.dto.response.VerificationCountResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.*;

class VerificationCountReadServiceTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOps;
    private VerificationCountQueryService queryService;

    private VerificationCountReadService service;

    private static final String KEY = "leafresh:totalVerifications:count";

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        queryService = mock(VerificationCountQueryService.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        service = new VerificationCountReadService(redisTemplate, queryService);
    }

    @Test
    @DisplayName("Redis 캐시에 값이 존재하면 해당 값을 반환한다")
    void getTotalVerificationCount_success_cacheHit() {
        when(valueOps.get(KEY)).thenReturn("42");

        VerificationCountResponseDto result = service.getTotalVerificationCount();

        assertThat(result.count()).isEqualTo(42);
        verify(queryService, never()).getTotalVerificationCountFromDB();
    }

    @Test
    @DisplayName("Redis 캐시에 없고 DB에서 조회하여 캐싱한다")
    void getTotalVerificationCount_cacheMiss_dbFallback() {
        when(valueOps.get(KEY)).thenReturn(null).thenReturn(null);
        when(queryService.getTotalVerificationCountFromDB()).thenReturn(99);

        VerificationCountResponseDto result = service.getTotalVerificationCount();

        assertThat(result.count()).isEqualTo(99);
        verify(queryService).getTotalVerificationCountFromDB();
        verify(valueOps).set(eq(KEY), eq("99"), eq(Duration.ofHours(24)));
    }

    @Test
    @DisplayName("Redis 캐시에 없고 락 도중 다른 인스턴스가 캐싱하면 그대로 반환")
    void getTotalVerificationCount_cacheMiss_but_cacheRestoredInLock() {
        when(valueOps.get(KEY)).thenReturn(null).thenReturn("55");

        VerificationCountResponseDto result = service.getTotalVerificationCount();

        assertThat(result.count()).isEqualTo(55);
        verify(queryService, never()).getTotalVerificationCountFromDB();
    }

    @Test
    @DisplayName("Redis 값이 숫자가 아니면 예외를 던진다")
    void getTotalVerificationCount_fail_invalidFormat() {
        when(valueOps.get(KEY)).thenReturn("not-a-number");

        CustomException exception = catchThrowableOfType(
                () -> service.getTotalVerificationCount(),
                CustomException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(VerificationErrorCode.VERIFICATION_COUNT_QUERY_FAILED);
    }

    @Test
    @DisplayName("Redis 조회 중 예외가 발생하면 예외를 던진다")
    void getTotalVerificationCount_fail_redisError() {
        when(valueOps.get(KEY)).thenThrow(new RuntimeException("Redis error"));

        CustomException exception = catchThrowableOfType(
                () -> service.getTotalVerificationCount(),
                CustomException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(VerificationErrorCode.VERIFICATION_COUNT_QUERY_FAILED);
    }
}
