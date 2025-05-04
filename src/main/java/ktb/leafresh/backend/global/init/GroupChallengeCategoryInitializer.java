package ktb.leafresh.backend.global.init;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Leafresh 서비스의 기본 GroupChallengeCategory 목록을 DB에 등록하는 초기화 클래스입니다.
 * - ZERO_WASTE, PLOGGING, CARBON_FOOTPRINT, ENERGY_SAVING, UPCYCLING, MEDIA, DIGITAL_CARBON, VEGAN, ETC
 * - 애플리케이션 시작 시 존재하지 않을 경우 자동 생성됩니다.
 *
 * 위치: global.init (전역 초기화 책임)
 */

@Component
@RequiredArgsConstructor
public class GroupChallengeCategoryInitializer implements CommandLineRunner {

    private final GroupChallengeCategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<GroupChallengeCategorySeed> seeds = List.of(
                new GroupChallengeCategorySeed("ZERO_WASTE", "제로웨이스트", "imageUrl1", 1),
                new GroupChallengeCategorySeed("PLOGGING", "플로깅", "imageUrl2", 2),
                new GroupChallengeCategorySeed("CARBON_FOOTPRINT", "탄소 발자국", "imageUrl3", 3),
                new GroupChallengeCategorySeed("ENERGY_SAVING", "에너지 절약", "imageUrl4", 4),
                new GroupChallengeCategorySeed("UPCYCLING", "중고거래/업사이클", "imageUrl5", 5),
                new GroupChallengeCategorySeed("MEDIA", "서적, 영화", "imageUrl6", 6),
                new GroupChallengeCategorySeed("DIGITAL_CARBON", "디지털 탄소", "imageUrl7", 7),
                new GroupChallengeCategorySeed("VEGAN", "비건", "imageUrl8", 8),
                new GroupChallengeCategorySeed("ETC", "기타", "imageUrl9", 9)
        );

        for (GroupChallengeCategorySeed seed : seeds) {
            if (categoryRepository.findByName(seed.name()).isEmpty()) {
                categoryRepository.save(
                        GroupChallengeCategory.builder()
                                .name(seed.name())
                                .imageUrl(seed.imageUrl())
                                .sequenceNumber(seed.sequenceNumber())
                                .activated(true)
                                .build()
                );
            }
        }
    }

    private record GroupChallengeCategorySeed(String name, String label, String imageUrl, int sequenceNumber) {}
}
