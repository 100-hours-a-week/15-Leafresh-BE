package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberLeafPointQueryRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.TotalLeafPointResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.LeafPointErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeafPointReadServiceTest {

    private StringRedisTemplate redisTemplate;
    private MemberLeafPointQueryRepository queryRepository;
    private RedissonClient redissonClient;
    private LeafPointReadService service;

    private ValueOperations<String, String> valueOperations;
    private RLock mockLock;

    private static final String REDIS_KEY = "leafresh:totalLeafPoints:sum";
    private static final String LOCK_KEY = "lock:leafresh:totalLeafPoints:sum";

    @BeforeEach
    void setUp() throws Exception {
        redisTemplate = mock(StringRedisTemplate.class);
        queryRepository = mock(MemberLeafPointQueryRepository.class);
        redissonClient = mock(RedissonClient.class);
        valueOperations = mock(ValueOperations.class);
        mockLock = mock(RLock.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redissonClient.getLock(LOCK_KEY)).thenReturn(mockLock);
        when(mockLock.tryLock(anyLong(), anyLong(), any())).thenReturn(true);

        service = new LeafPointReadService(redisTemplate, queryRepository, redissonClient);
    }

    @Test
    @DisplayName("Redis 캐시에 값이 있으면 캐시에서 반환한다")
    void getTotalLeafPoints_cacheHit() {
        // given
        when(valueOperations.get(REDIS_KEY)).thenReturn("12345");

        // when
        TotalLeafPointResponseDto result = service.getTotalLeafPoints();

        // then
        assertThat(result.count()).isEqualTo(12345);
        verify(queryRepository, never()).getTotalLeafPointSum();
    }

    @Test
    @DisplayName("Redis 캐시에 없으면 DB 조회 후 캐싱한다")
    void getTotalLeafPoints_cacheMiss() {
        // given
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(queryRepository.getTotalLeafPointSum()).thenReturn(9876);

        // when
        TotalLeafPointResponseDto result = service.getTotalLeafPoints();

        // then
        assertThat(result.count()).isEqualTo(9876);
        verify(valueOperations).set(REDIS_KEY, "9876", Duration.ofHours(24));
    }

    @Test
    @DisplayName("Redis 값이 숫자 변환 불가일 경우 예외 발생")
    void getTotalLeafPoints_redisFormatError() {
        // given
        when(valueOperations.get(REDIS_KEY)).thenReturn("not-a-number");

        // when
        CustomException ex = catchThrowableOfType(
                () -> service.getTotalLeafPoints(),
                CustomException.class
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(LeafPointErrorCode.REDIS_FAILURE);
    }

    @Test
    @DisplayName("DB 조회 중 예외 발생 시 CustomException 반환")
    void getTotalLeafPoints_dbError() {
        when(valueOperations.get(REDIS_KEY)).thenReturn(null);
        when(queryRepository.getTotalLeafPointSum()).thenThrow(new RuntimeException("DB 장애"));

        CustomException ex = catchThrowableOfType(
                () -> service.getTotalLeafPoints(),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(LeafPointErrorCode.DB_QUERY_FAILED);
    }
}
