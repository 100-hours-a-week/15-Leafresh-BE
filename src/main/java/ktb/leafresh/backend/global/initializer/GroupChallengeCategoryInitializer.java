package ktb.leafresh.backend.global.initializer;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.enums.GroupChallengeCategoryName;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Leafresh 서비스의 기본 GroupChallengeCategory 목록을 DB에 등록하는 초기화 클래스입니다. - ZERO_WASTE, PLOGGING,
 * CARBON_FOOTPRINT, ENERGY_SAVING, UPCYCLING, MEDIA, DIGITAL_CARBON, VEGAN, ETC - 애플리케이션 시작 시 존재하지
 * 않을 경우 자동 생성됩니다.
 *
 * <p>위치: global.init (전역 초기화 책임)
 */
@Component
@RequiredArgsConstructor
public class GroupChallengeCategoryInitializer implements CommandLineRunner {

  private final GroupChallengeCategoryRepository categoryRepository;

  @Override
  @Transactional
  public void run(String... args) {
    for (GroupChallengeCategoryName seed : GroupChallengeCategoryName.seeds()) {
      String name = seed.name();
      if (categoryRepository.findByName(name).isEmpty()) {
        categoryRepository.save(
            GroupChallengeCategory.builder()
                .name(name)
                .imageUrl(GroupChallengeCategoryName.getImageUrl(name))
                .sequenceNumber(GroupChallengeCategoryName.getSequence(name))
                .activated(true)
                .build());
      }
    }
  }
}
