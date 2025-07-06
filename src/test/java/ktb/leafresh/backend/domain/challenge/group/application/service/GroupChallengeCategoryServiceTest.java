package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeCategoryResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupChallengeCategoryService 테스트")
class GroupChallengeCategoryServiceTest {

    @Mock
    private GroupChallengeCategoryRepository categoryRepository;

    @InjectMocks
    private GroupChallengeCategoryService categoryService;

    private GroupChallengeCategory category1;
    private GroupChallengeCategory category2;
    private GroupChallengeCategory etcCategory;

    @BeforeEach
    void setUp() {
        category1 = of("ZERO_WASTE");
        category2 = of("PLOGGING");
        etcCategory = of("ETC");
    }

    @Test
    @DisplayName("활성화된 ETC 제외 카테고리 목록을 조회할 수 있다")
    void getCategories_withValidCategories_returnsList() {
        // given
        given(categoryRepository.findAllByActivatedIsTrueOrderBySequenceNumberAsc())
                .willReturn(List.of(category1, category2, etcCategory));

        // when
        List<GroupChallengeCategoryResponseDto> result = categoryService.getCategories();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("category").containsExactlyInAnyOrder("ZERO_WASTE", "PLOGGING");
        assertThat(result).extracting("label").contains("제로웨이스트", "플로깅");
    }

    @Test
    @DisplayName("ETC 제외 후 카테고리가 없으면 예외가 발생한다")
    void getCategories_whenFilteredEmpty_throwsException() {
        // given
        given(categoryRepository.findAllByActivatedIsTrueOrderBySequenceNumberAsc())
                .willReturn(List.of(etcCategory));

        // when & then
        assertThatThrownBy(() -> categoryService.getCategories())
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.CHALLENGE_CATEGORY_LIST_EMPTY.getMessage());
    }

    @Test
    @DisplayName("카테고리 조회 중 예외 발생 시 CustomException으로 변환된다")
    void getCategories_whenRepositoryFails_throwsWrappedCustomException() {
        // given
        given(categoryRepository.findAllByActivatedIsTrueOrderBySequenceNumberAsc())
                .willThrow(new RuntimeException("DB 오류"));

        // when & then
        assertThatThrownBy(() -> categoryService.getCategories())
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.CHALLENGE_CATEGORY_READ_FAILED.getMessage());
    }
}
