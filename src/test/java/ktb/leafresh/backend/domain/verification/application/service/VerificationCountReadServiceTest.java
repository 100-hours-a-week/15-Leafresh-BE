package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.presentation.dto.response.VerificationCountResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.*;

class VerificationCountReadServiceTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOps;
    private VerificationCountQueryService queryService;
    private RedissonClient redissonClient;
    private RLock mockLock;

    private VerificationCountReadService service;

    private static final String KEY = "leafresh:totalVerifications:count";
    private static final String LOCK_KEY = "lock:leafresh:totalVerifications:count";

    @BeforeEach
    void setUp() throws Exception {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        queryService = mock(VerificationCountQueryService.class);
        redissonClient = mock(RedissonClient.class);
        mockLock = mock(RLock.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(redissonClient.getLock(LOCK_KEY)).thenReturn(mockLock);
        when(mockLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);

        service = new VerificationCountReadService(redisTemplate, queryService, redissonClient);
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
        verify(queryService, never()).getTotalVerificationCountFromDB();
    }

    @Test
    @DisplayName("Redis 캐시에 없고 락 획득 후 DB에서 조회하여 캐싱한다")
    void getTotalVerificationCount_cacheMiss_dbFallback() {
        // given
        when(valueOps.get(KEY)).thenReturn(null);
        when(queryService.getTotalVerificationCountFromDB()).thenReturn(99);

        // when
        VerificationCountResponseDto result = service.getTotalVerificationCount();

        // then
        assertThat(result.count()).isEqualTo(99);
        verify(queryService).getTotalVerificationCountFromDB();
        verify(valueOps).set(eq(KEY), eq("99"), any());
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

    @Test
    @DisplayName("락 획득 실패 시 fallback으로 Redis 재조회 후 반환")
    void getTotalVerificationCount_lockFail_fallbackRedis() throws Exception {
        when(mockLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);
        when(valueOps.get(KEY)).thenReturn(null).thenReturn("55");

        VerificationCountResponseDto result = service.getTotalVerificationCount();

        assertThat(result.count()).isEqualTo(55);
        verify(queryService, never()).getTotalVerificationCountFromDB();
    }
}
