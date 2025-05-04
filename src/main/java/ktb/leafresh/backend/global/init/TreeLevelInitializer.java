package ktb.leafresh.backend.global.init;

import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.domain.entity.enums.TreeLevelName;
import ktb.leafresh.backend.domain.member.infrastructure.repository.TreeLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Leafresh 서비스의 기본 TreeLevel 5단계를 DB에 등록하는 초기화 클래스입니다.
 * - SPROUT, YOUNG, SMALL_TREE, TREE, BIG_TREE
 * - 애플리케이션 시작 시 존재하지 않을 경우 자동 생성됩니다.
 *
 * 위치: global.init (전역 초기화 책임)
 */

@Component
@RequiredArgsConstructor
public class TreeLevelInitializer implements CommandLineRunner {
    private final TreeLevelRepository treeLevelRepository;

    @Override
    @Transactional
    public void run(String... args) {
        for (TreeLevelName levelName : TreeLevelName.values()) {
            if (treeLevelRepository.findByName(levelName).isEmpty()) {
                treeLevelRepository.save(
                        TreeLevel.builder()
                                .name(levelName)
                                .minLeafPoint(getMinLeafPoint(levelName))
                                .imageUrl("default_" + levelName.name().toLowerCase() + ".png")
                                .description(getDescription(levelName))
                                .build()
                );
            }
        }
    }

    private int getMinLeafPoint(TreeLevelName name) {
        return switch (name) {
            case SPROUT -> 0;
            case YOUNG -> 2000;
            case SMALL_TREE -> 4000;
            case TREE -> 6000;
            case BIG_TREE -> 8000;
        };
    }

    private String getDescription(TreeLevelName name) {
        return switch (name) {
            case SPROUT -> "씨앗 단계입니다.";
            case YOUNG -> "묘목 단계입니다.";
            case SMALL_TREE -> "작은 나무 단계입니다.";
            case TREE -> "성장한 나무 단계입니다.";
            case BIG_TREE -> "울창한 나무 단계입니다.";
        };
    }
}
