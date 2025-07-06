package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.enums.GroupChallengeCategoryName;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeSearchQueryRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeSummaryResponseDto;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupChallengeSearchReadService 테스트")
class GroupChallengeSearchReadServiceTest {

    @Mock
    private GroupChallengeSearchQueryRepository searchRepository;

    @InjectMocks
    private GroupChallengeSearchReadService searchReadService;

    @Nested
    @DisplayName("getGroupChallenges()는")
    class GetGroupChallenges {

        @Test
        @DisplayName("검색어와 카테고리 없이 챌린지 목록을 조회하고 DTO로 매핑한다.")
        void returnsChallengeListWithoutFilters() {
            // given
            var member = MemberFixture.of();
            var category = GroupChallengeCategoryFixture.defaultCategory();
            var challenge = GroupChallengeFixture.of(member, category);
            ReflectionTestUtils.setField(challenge, "id", 1L);
            ReflectionTestUtils.setField(challenge, "createdAt", challenge.getStartDate());

            given(searchRepository.findByFilter(null, null, null, null, 6))
                    .willReturn(List.of(challenge));

            // when
            CursorPaginationResult<GroupChallengeSummaryResponseDto> result = searchReadService
                    .getGroupChallenges(null, null, null, null, 5);

            // then
            assertThat(result).isNotNull();
            assertThat(result.items()).hasSize(1);
            assertThat(result.hasNext()).isFalse();
            assertThat(result.cursorInfo()).isNotNull();
            assertThat(result.items().get(0).title()).isEqualTo("제로웨이스트 챌린지");
        }

        @Test
        @DisplayName("카테고리 필터가 있을 경우 category name으로 필터링이 적용된다.")
        void filtersByCategory() {
            // given
            var member = MemberFixture.of();
            var category = GroupChallengeCategoryFixture.of(GroupChallengeCategoryName.VEGAN);
            var challenge = GroupChallengeFixture.of(member, category);
            ReflectionTestUtils.setField(challenge, "id", 2L);
            ReflectionTestUtils.setField(challenge, "createdAt", challenge.getStartDate());

            given(searchRepository.findByFilter(null, "VEGAN", null, null, 6))
                    .willReturn(List.of(challenge));

            // when
            var result = searchReadService.getGroupChallenges(null, GroupChallengeCategoryName.VEGAN, null, null, 5);

            // then
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).category()).isEqualTo("VEGAN");
        }

        @Test
        @DisplayName("조회된 결과 수가 요청 수보다 많으면 hasNext가 true다.")
        void hasNextIsTrueWhenResultsExceedPageSize() {
            // given
            var member = MemberFixture.of();
            var category = GroupChallengeCategoryFixture.defaultCategory();

            var challenge1 = GroupChallengeFixture.of(member, category);
            var challenge2 = GroupChallengeFixture.of(member, category);
            var challenge3 = GroupChallengeFixture.of(member, category);
            var challenge4 = GroupChallengeFixture.of(member, category);

            ReflectionTestUtils.setField(challenge1, "id", 1L);
            ReflectionTestUtils.setField(challenge2, "id", 2L);
            ReflectionTestUtils.setField(challenge3, "id", 3L);
            ReflectionTestUtils.setField(challenge4, "id", 4L);
            ReflectionTestUtils.setField(challenge1, "createdAt", challenge1.getStartDate());
            ReflectionTestUtils.setField(challenge2, "createdAt", challenge2.getStartDate());
            ReflectionTestUtils.setField(challenge3, "createdAt", challenge3.getStartDate());
            ReflectionTestUtils.setField(challenge4, "createdAt", challenge4.getStartDate());

            given(searchRepository.findByFilter(null, null, null, null, 4))
                    .willReturn(List.of(challenge1, challenge2, challenge3, challenge4));

            // when
            var result = searchReadService.getGroupChallenges(null, null, null, null, 3);

            // then
            assertThat(result.items()).hasSize(3);
            assertThat(result.hasNext()).isTrue();
        }
    }
}
