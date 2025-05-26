package ktb.leafresh.backend.global.initializer;

import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.domain.entity.enums.TreeLevelName;
import ktb.leafresh.backend.domain.member.infrastructure.repository.TreeLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
                                .imageUrl(getImageUrl(levelName))
                                .description(getDescription(levelName))
                                .build()
                );
            }
        }
    }

    private int getMinLeafPoint(TreeLevelName name) {
        return switch (name) {
            case SPROUT -> 0;           // 새싹
            case YOUNG -> 2500;         // 묘목
            case SMALL_TREE -> 5000;    // 작은 나무
            case TREE -> 7500;          // 중간 나무
            case BIG_TREE -> 10000;     // 큰 나무
        };
    }

    private String getImageUrl(TreeLevelName name) {
        return "https://storage.googleapis.com/leafresh-images/init/treelevel/" + name.name() + ".png";
    }

    private String getDescription(TreeLevelName name) {
        return switch (name) {
            case SPROUT -> "새싹 단계입니다.";
            case YOUNG -> "묘목 단계입니다.";
            case SMALL_TREE -> "작은 나무 단계입니다.";
            case TREE -> "중간 나무 단계입니다.";
            case BIG_TREE -> "큰 나무 단계입니다.";
        };
    }
}
