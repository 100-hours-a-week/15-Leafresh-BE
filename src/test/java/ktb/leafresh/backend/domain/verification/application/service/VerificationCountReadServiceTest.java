package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.presentation.dto.response.VerificationCountResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationCountReadServiceTest {

  @Mock private StringRedisTemplate redisTemplate;

  @Mock private VerificationCountQueryService verificationCountQueryService;

  @Mock private ValueOperations<String, String> valueOperations;

  @InjectMocks private VerificationCountReadService readService;

  private static final String KEY = "leafresh:totalVerifications:count";

  @Test
  @DisplayName("캐시 hit 시 - Redis 값 사용하여 count 반환")
  void getTotalVerificationCount_cacheHit() {
    // given
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.get(KEY)).willReturn("123");

    // when
    VerificationCountResponseDto result = readService.getTotalVerificationCount();

    // then
    assertThat(result.count()).isEqualTo(123);
    verify(redisTemplate.opsForValue()).get(KEY);
    verifyNoInteractions(verificationCountQueryService);
  }

  @Test
  @DisplayName("캐시 miss 시 - DB에서 조회하여 반환 (withLock)")
  void getTotalVerificationCount_cacheMiss_thenDBQuery() {
    // given
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.get(KEY)).willReturn(null); // 1차 조회 캐시 miss
    given(verificationCountQueryService.getTotalVerificationCountFromDB()).willReturn(999);

    // 2차 조회도 miss 처리하여 DB 값으로 캐싱
    given(valueOperations.get(KEY)).willReturn(null);

    // when
    VerificationCountResponseDto result = readService.getTotalVerificationCount();

    // then
    assertThat(result.count()).isEqualTo(999);
    verify(verificationCountQueryService).getTotalVerificationCountFromDB();
    verify(valueOperations).set(KEY, "999", Duration.ofHours(24));
  }

  @Test
  @DisplayName("캐시 파싱 오류 시 - 예외 발생")
  void getTotalVerificationCount_cacheParseError() {
    // given
    given(redisTemplate.opsForValue()).willReturn(valueOperations);
    given(valueOperations.get(KEY)).willReturn("not-a-number");

    // when & then
    assertThatThrownBy(() -> readService.getTotalVerificationCount())
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(VerificationErrorCode.VERIFICATION_COUNT_QUERY_FAILED.getMessage());
  }

  @Test
  @DisplayName("Redis 조회 중 예외 발생 시 - CustomException 반환")
  void getTotalVerificationCount_redisException() {
    // given
    given(redisTemplate.opsForValue()).willThrow(new RuntimeException("Redis down"));

    // when & then
    assertThatThrownBy(() -> readService.getTotalVerificationCount())
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(VerificationErrorCode.VERIFICATION_COUNT_QUERY_FAILED.getMessage());
  }
}
