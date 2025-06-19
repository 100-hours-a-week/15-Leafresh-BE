package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberLeafPointQueryRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.TotalLeafPointResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.LeafPointErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LeafPointReadServiceTest {

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOps;
    private MemberLeafPointQueryRepository queryRepository;
    private LeafPointReadService service;

    private static final String REDIS_KEY = "leafresh:totalLeafPoints:sum";

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOps = mock(ValueOperations.class);
        queryRepository = mock(MemberLeafPointQueryRepository.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        service = new LeafPointReadService(redisTemplate, queryRepository);
    }

    @Test
    @DisplayName("Redis에 값이 있으면 그대로 반환한다")
    void cacheHit() {
        when(valueOps.get(REDIS_KEY)).thenReturn("1000");

        TotalLeafPointResponseDto result = service.getTotalLeafPoints();

        assertThat(result.count()).isEqualTo(1000);
        verify(queryRepository, never()).getTotalLeafPointSum();
    }

    @Test
    @DisplayName("Redis 캐시에 없으면 DB 조회 후 캐싱한다")
    void getTotalLeafPoints_cacheMiss() {
        // given
        when(valueOps.get(REDIS_KEY)).thenReturn(null).thenReturn(null);
        when(queryRepository.getTotalLeafPointSum()).thenReturn(9876);

        ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);

        // when
        TotalLeafPointResponseDto result = service.getTotalLeafPoints();

        // then
        assertThat(result.count()).isEqualTo(9876);

        verify(valueOps).set(eq(REDIS_KEY), eq("9876"), durationCaptor.capture());
        assertThat(durationCaptor.getValue()).isEqualTo(Duration.ofHours(24));
    }

    @Test
    @DisplayName("Redis에 값이 없으면 DB 조회 후 바로 캐싱한다")
    void cacheMiss_thenGoToDbImmediately() {
        when(valueOps.get(REDIS_KEY)).thenReturn(null);
        when(queryRepository.getTotalLeafPointSum()).thenReturn(5555);

        TotalLeafPointResponseDto result = service.getTotalLeafPoints();

        assertThat(result.count()).isEqualTo(5555);
        verify(queryRepository).getTotalLeafPointSum();
    }

    @Test
    @DisplayName("Redis 값이 숫자로 변환 불가능하면 예외 발생")
    void redisFormatError() {
        when(valueOps.get(REDIS_KEY)).thenReturn("not-a-number");

        CustomException ex = catchThrowableOfType(
                () -> service.getTotalLeafPoints(),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(LeafPointErrorCode.REDIS_FAILURE);
    }

    @Test
    @DisplayName("Redis 조회 중 예외가 발생하면 DB_QUERY_FAILED로 감싼 예외 반환")
    void redisExceptionThrown() {
        when(valueOps.get(REDIS_KEY)).thenThrow(new RuntimeException("Redis down"));

        CustomException ex = catchThrowableOfType(
                () -> service.getTotalLeafPoints(),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(LeafPointErrorCode.DB_QUERY_FAILED);
    }

    @Test
    @DisplayName("DB 조회 중 예외 발생 시 CustomException 반환")
    void dbExceptionThrown() {
        when(valueOps.get(REDIS_KEY)).thenReturn(null).thenReturn(null);
        when(queryRepository.getTotalLeafPointSum()).thenThrow(new RuntimeException("DB 장애"));

        CustomException ex = catchThrowableOfType(
                () -> service.getTotalLeafPoints(),
                CustomException.class
        );

        assertThat(ex.getErrorCode()).isEqualTo(LeafPointErrorCode.DB_QUERY_FAILED);
    }
}
