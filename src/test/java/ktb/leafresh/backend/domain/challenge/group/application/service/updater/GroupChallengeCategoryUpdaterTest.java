package ktb.leafresh.backend.domain.challenge.group.application.service.updater;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupChallengeCategoryUpdater 테스트")
class GroupChallengeCategoryUpdaterTest {

  @Mock private GroupChallengeCategoryRepository categoryRepository;

  @InjectMocks private GroupChallengeCategoryUpdater categoryUpdater;

  @Test
  @DisplayName("카테고리를 성공적으로 변경할 수 있다")
  void updateCategory_withValidCategoryName_updatesCategory() {
    // given
    Member member = MemberFixture.of();
    GroupChallengeCategory originalCategory = GroupChallengeCategoryFixture.of("ZERO_WASTE");
    GroupChallenge challenge = GroupChallengeFixture.of(member, originalCategory);

    GroupChallengeCategory newCategory = GroupChallengeCategoryFixture.of("VEGAN");

    given(categoryRepository.findByName("VEGAN")).willReturn(Optional.of(newCategory));

    // when
    categoryUpdater.updateCategory(challenge, "VEGAN");

    // then
    assertThat(challenge.getCategory()).isEqualTo(newCategory);
  }

  @Test
  @DisplayName("존재하지 않는 카테고리 이름으로 변경 시 예외가 발생한다")
  void updateCategory_withInvalidCategoryName_throwsException() {
    // given
    Member member = MemberFixture.of();
    GroupChallengeCategory category = GroupChallengeCategoryFixture.of("ZERO_WASTE");
    GroupChallenge challenge = GroupChallengeFixture.of(member, category);

    String invalidCategoryName = "NOT_EXIST";

    given(categoryRepository.findByName(invalidCategoryName)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> categoryUpdater.updateCategory(challenge, invalidCategoryName))
        .isInstanceOf(CustomException.class)
        .hasMessageContaining(ChallengeErrorCode.CHALLENGE_CATEGORY_NOT_FOUND.getMessage());
  }
}
