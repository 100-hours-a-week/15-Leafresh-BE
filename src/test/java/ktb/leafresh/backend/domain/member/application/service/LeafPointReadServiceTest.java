package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberLeafPointQueryRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.TotalLeafPointResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.LeafPointErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class LeafPointReadServiceTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private MemberLeafPointQueryRepository memberLeafPointQueryRepository;

    @InjectMocks private LeafPointReadService leafPointReadService;

    private static final String KEY = "leafresh:totalLeafPoints:sum";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Redis 캐시가 존재하면 DB 조회 없이 반환")
    void getTotalLeafPoints_cacheHit() {
        // given
        given(valueOperations.get(KEY)).willReturn("12345");

        // when
        TotalLeafPointResponseDto response = leafPointReadService.getTotalLeafPoints();

        // then
        assertThat(response).isNotNull();
        assertThat(response.count()).isEqualTo(12345);
        verify(memberLeafPointQueryRepository, never()).getTotalLeafPointSum();
    }

    @Test
    @DisplayName("Redis 캐시가 없으면 DB에서 조회 후 캐시 저장")
    void getTotalLeafPoints_cacheMiss_thenQueryDb() {
        // given
        given(valueOperations.get(KEY)).willReturn(null);
        given(memberLeafPointQueryRepository.getTotalLeafPointSum()).willReturn(54321);

        // when
        TotalLeafPointResponseDto response = leafPointReadService.getTotalLeafPoints();

        // then
        assertThat(response.count()).isEqualTo(54321);
        verify(valueOperations).set(KEY, "54321", Duration.ofHours(24));
    }

    @Test
    @DisplayName("Redis 값이 숫자가 아니면 예외 발생")
    void getTotalLeafPoints_invalidRedisValue() {
        // given
        given(valueOperations.get(KEY)).willReturn("invalid_number");

        // then
        assertThatThrownBy(() -> leafPointReadService.getTotalLeafPoints())
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(LeafPointErrorCode.REDIS_FAILURE.getMessage());
    }

    @Test
    @DisplayName("DB 조회 중 알 수 없는 예외 발생 시 CustomException 반환")
    void getTotalLeafPoints_dbQueryFails() {
        // given
        given(valueOperations.get(KEY)).willReturn(null);
        given(memberLeafPointQueryRepository.getTotalLeafPointSum()).willThrow(new RuntimeException("DB connection failed"));

        // then
        assertThatThrownBy(() -> leafPointReadService.getTotalLeafPoints())
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(LeafPointErrorCode.DB_QUERY_FAILED.getMessage());
    }
}