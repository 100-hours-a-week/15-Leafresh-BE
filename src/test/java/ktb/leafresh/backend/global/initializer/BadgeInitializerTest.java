package ktb.leafresh.backend.global.initializer;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.enums.BadgeType;
import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
import ktb.leafresh.backend.support.fixture.BadgeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BadgeInitializer 테스트")
class BadgeInitializerTest {

    @Mock
    private BadgeRepository badgeRepository;

    @InjectMocks
    private BadgeInitializer badgeInitializer;

    @Test
    @DisplayName("뱃지가 존재하지 않으면 저장된다")
    void run_whenBadgeNotExists_thenSavesBadge() throws Exception {
        // given
        String badgeName = "제로 히어로";
        given(badgeRepository.findByName(badgeName)).willReturn(Optional.empty());

        // when
        badgeInitializer.run();

        // then
        then(badgeRepository).should(atLeastOnce()).save(argThat(badge ->
                badge.getName().equals(badgeName)
                        && badge.getType() == BadgeType.GROUP
                        && badge.getCondition() != null
                        && badge.getImageUrl() != null
        ));
    }

    @Test
    @DisplayName("이미 존재하는 뱃지는 저장되지 않는다")
    void run_whenBadgeExists_thenDoesNotSave() throws Exception {
        // given
        String badgeName = "제로 히어로";
        Badge existingBadge = BadgeFixture.of(badgeName, BadgeType.GROUP);
        given(badgeRepository.findByName(badgeName)).willReturn(Optional.of(existingBadge));

        // when
        badgeInitializer.run();

        // then
        then(badgeRepository).should(never()).save(existingBadge);
    }
}
