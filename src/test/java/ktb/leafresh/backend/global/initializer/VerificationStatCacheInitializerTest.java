package ktb.leafresh.backend.global.initializer;

import ktb.leafresh.backend.domain.verification.infrastructure.cache.VerificationStatCacheService;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.CommentRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.LikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerificationStatCacheInitializer 테스트")
class VerificationStatCacheInitializerTest {

    @Mock
    private GroupChallengeVerificationRepository verificationRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private VerificationStatCacheService verificationStatCacheService;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @InjectMocks
    private VerificationStatCacheInitializer initializer;

    @Test
    @DisplayName("Redis에 캐시가 없는 경우 - 인증 ID별 캐시 초기화가 수행된다")
    void initializeStats_whenNoRedisKeys_thenInitialize() {
        // given
        Object[] viewRow = new Object[]{1L, 10};
        Object[] likeRow = new Object[]{1L, 5};
        Object[] commentRow = new Object[]{1L, 2};

        given(verificationRepository.findAllViewCountByVerificationId())
                .willReturn(List.<Object[]>of(viewRow));

        given(likeRepository.findAllLikeCountByVerificationId())
                .willReturn(List.<Object[]>of(likeRow));

        given(commentRepository.findAllCommentCountByVerificationId())
                .willReturn(List.<Object[]>of(commentRow));

        given(stringRedisTemplate.hasKey("verification:stat:1")).willReturn(false);

        // when
        initializer.initializeVerificationStats();

        // then
        then(verificationStatCacheService)
                .should().initializeVerificationStats(1L, 10, 5, 2);
    }

    @Test
    @DisplayName("Redis에 캐시가 이미 존재하는 경우 - 초기화를 수행하지 않는다")
    void initializeStats_whenRedisKeyExists_thenSkipInitialization() {
        // given
        Object[] viewRow = new Object[]{1L, 10};

        given(verificationRepository.findAllViewCountByVerificationId())
                .willReturn(List.<Object[]>of(viewRow));
        given(likeRepository.findAllLikeCountByVerificationId()).willReturn(List.of());
        given(commentRepository.findAllCommentCountByVerificationId()).willReturn(List.of());

        given(stringRedisTemplate.hasKey("verification:stat:1")).willReturn(true);

        // when
        initializer.initializeVerificationStats();

        // then
        then(verificationStatCacheService)
                .should(never()).initializeVerificationStats(anyLong(), anyInt(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("뷰/좋아요/댓글이 일부 없는 경우에도 기본값 0으로 캐시가 초기화된다")
    void initializeStats_withMissingMetrics_thenUseDefaultValues() {
        // given
        Object[] viewRow = new Object[]{1L, null}; // null 값 존재
        // like, comment는 아예 없음

        given(verificationRepository.findAllViewCountByVerificationId())
                .willReturn(List.<Object[]>of(viewRow));
        given(likeRepository.findAllLikeCountByVerificationId()).willReturn(List.of());
        given(commentRepository.findAllCommentCountByVerificationId()).willReturn(List.of());

        given(stringRedisTemplate.hasKey("verification:stat:1")).willReturn(false);

        // when
        initializer.initializeVerificationStats();

        // then
        then(verificationStatCacheService)
                .should().initializeVerificationStats(1L, 0, 0, 0); // null → 0 처리 검증
    }

    @Test
    @DisplayName("캐시 초기화 도중 예외가 발생해도 다른 ID의 초기화는 계속 진행된다")
    void initializeStats_withException_thenContinueOthers() {
        // given
        Object[] viewRow1 = new Object[]{1L, 10};
        Object[] viewRow2 = new Object[]{2L, 20};

        given(verificationRepository.findAllViewCountByVerificationId()).willReturn(List.of(viewRow1, viewRow2));
        given(likeRepository.findAllLikeCountByVerificationId()).willReturn(List.of());
        given(commentRepository.findAllCommentCountByVerificationId()).willReturn(List.of());

        given(stringRedisTemplate.hasKey("verification:stat:1")).willReturn(false);
        given(stringRedisTemplate.hasKey("verification:stat:2")).willReturn(false);

        willThrow(new RuntimeException("DB Error")).given(verificationStatCacheService)
                .initializeVerificationStats(eq(1L), anyInt(), anyInt(), anyInt());

        // when
        initializer.initializeVerificationStats();

        // then
        then(verificationStatCacheService)
                .should().initializeVerificationStats(2L, 20, 0, 0);
    }
}
