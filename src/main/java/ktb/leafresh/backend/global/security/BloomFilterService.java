package ktb.leafresh.backend.global.security;

import io.rebloom.client.Client;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Bloom Filter 초기화 및 관리 서비스
 * False Positive 확률과 예상 삽입 수를 설정파일에서 읽어와 적용
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Getter
@Profile("!swagger")
public class BloomFilterService {

    private final Client bloomClient;

    @Value("${jwt.bloom-filter.key:accessTokenBlacklist}")
    private String filterName;

    @Value("${jwt.bloom-filter.expected-insertions:100000}")
    private long expectedInsertions;

    @Value("${jwt.bloom-filter.false-positive-probability:0.0001}")
    private double falsePositiveProbability;

    @PostConstruct
    public void initializeBloomFilter() {
        try {
            // Bloom Filter 존재 여부 확인
            if (!isFilterExists()) {
                log.info("Bloom Filter 초기화 시작 - name: {}, expectedInsertions: {}, falsePositiveProbability: {}", 
                    filterName, expectedInsertions, falsePositiveProbability);
                
                // BF.RESERVE 명령으로 Bloom Filter 생성 (확률과 용량 설정)
                bloomClient.createFilter(filterName, expectedInsertions, falsePositiveProbability);
                
                log.info("Bloom Filter 초기화 완료 - 예상 False Positive 비율: {}%", 
                    falsePositiveProbability * 100);
            } else {
                log.info("기존 Bloom Filter 사용 - name: {}", filterName);
                logFilterInfo();
            }
        } catch (Exception e) {
            log.error("Bloom Filter 초기화 실패", e);
            throw new RuntimeException("Bloom Filter 초기화 실패", e);
        }
    }

    /**
     * Bloom Filter 존재 여부 확인
     */
    private boolean isFilterExists() {
        try {
            // BF.INFO 명령으로 필터 정보 확인
            bloomClient.info(filterName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Bloom Filter 정보 로깅
     */
    private void logFilterInfo() {
        try {
            var info = bloomClient.info(filterName);
            log.info("현재 Bloom Filter 상태: {}", info);
        } catch (Exception e) {
            log.warn("Bloom Filter 정보 조회 실패", e);
        }
    }

    /**
     * 토큰을 Bloom Filter에 추가
     */
    public void addToken(String token) {
        try {
            bloomClient.add(filterName, token);
            log.debug("토큰 추가됨 - token: {}", token);
        } catch (Exception e) {
            log.error("토큰 추가 실패 - token: {}", token, e);
            throw new RuntimeException("Bloom Filter 토큰 추가 실패", e);
        }
    }

    /**
     * 토큰이 Bloom Filter에 존재할 가능성 확인
     */
    public boolean mightContain(String token) {
        try {
            return bloomClient.exists(filterName, token);
        } catch (Exception e) {
            log.error("토큰 존재 확인 실패 - token: {}", token, e);
            // 안전을 위해 true 반환 (Redis에서 다시 확인)
            return true;
        }
    }
}
