package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import jakarta.persistence.EntityManager;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.domain.entity.enums.TreeLevelName;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.global.config.QuerydslConfig;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GroupChallengeSearchQueryRepositoryImpl 통합 테스트
 * 
 * 빅테크 기업의 테스트 Best Practice를 따름:
 * - 명확한 테스트 이름 (Given-When-Then 패턴)
 * - 독립적인 테스트 데이터 설정
 * - Repository를 활용한 실제 DB 테스트
 * - 엣지 케이스 포함
 */
@DataJpaTest
@Import(QuerydslConfig.class)
@ActiveProfiles("test")
@DisplayName("GroupChallengeSearchQueryRepositoryImpl 통합 테스트")
class GroupChallengeSearchQueryRepositoryImplTest {

    @Autowired
    private EntityManager em;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private GroupChallengeRepository challengeRepository;
    
    private GroupChallengeSearchQueryRepositoryImpl queryRepository;
    
    private Member testMember;
    private GroupChallengeCategory environmentCategory;
    private GroupChallengeCategory healthCategory;

    @BeforeEach
    void setUp() {
        queryRepository = new GroupChallengeSearchQueryRepositoryImpl(
            new com.querydsl.jpa.impl.JPAQueryFactory(em)
        );
        
        // 테스트 데이터 초기화
        TreeLevel treeLevel = TreeLevel.builder()
            .name(TreeLevelName.SPROUT)
            .minLeafPoint(0)
            .imageUrl("https://dummy.image/tree/sprout.png")
            .description("새싹 레벨")
            .build();
        em.persist(treeLevel);
        
        testMember = MemberFixture.of("test@example.com", "테스터");
        ReflectionTestUtils.setField(testMember, "treeLevel", treeLevel);
        testMember = memberRepository.save(testMember);
        
        environmentCategory = GroupChallengeCategoryFixture.of("환경");
        healthCategory = GroupChallengeCategoryFixture.of("건강");
        em.persist(environmentCategory);
        em.persist(healthCategory);
        
        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("findByFilter 메서드 테스트")
    class FindByFilterTest {

        @Test
        @DisplayName("findByFilter - 필터 조건 없이 모든 유효한 챌린지를 조회한다")
        void findByFilter_WhenNoFilter_ShouldReturnAllValidChallenges() {
            // given
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            GroupChallenge activeChallenge = createActiveChallenge("활성 챌린지", environmentCategory, now);
            GroupChallenge endedChallenge = createEndedChallenge("종료된 챌린지", environmentCategory, now);
            GroupChallenge eventChallenge = createEventChallenge("이벤트 챌린지", environmentCategory, now);
            
            // when
            List<GroupChallenge> results = queryRepository.findByFilter(null, null, null, null, 10);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(activeChallenge.getId());
        }

        @Test
        @DisplayName("findByFilter - 제목이나 설명에 검색어가 포함된 챌린지를 조회한다")
        void findByFilter_WhenSearchByTitleOrDescription_ShouldReturnMatchingChallenges() {
            // given
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            GroupChallenge titleMatch = createActiveChallenge("환경보호 챌린지", environmentCategory, now);
            GroupChallenge descriptionMatch = createActiveChallengeWithDescription(
                "일상 챌린지", "환경보호를 위한 실천", environmentCategory, now);
            GroupChallenge noMatch = createActiveChallenge("건강 챌린지", healthCategory, now);
            
            // when
            List<GroupChallenge> results = queryRepository.findByFilter("환경보호", null, null, null, 10);

            // then
            assertThat(results).hasSize(2);
            assertThat(results).extracting("id")
                .containsExactlyInAnyOrder(titleMatch.getId(), descriptionMatch.getId());
        }

        @Test
        @DisplayName("findByFilter - 대소문자 구분 없이 검색한다")
        void findByFilter_WhenSearchCaseInsensitive_ShouldReturnMatchingChallenges() {
            // given
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            GroupChallenge challenge = createActiveChallenge("ENVIRONMENTAL Challenge", environmentCategory, now);
            
            // when
            List<GroupChallenge> results = queryRepository.findByFilter("environmental", null, null, null, 10);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(challenge.getId());
        }

        @Test
        @DisplayName("findByFilter - 카테고리로 필터링이 올바르게 동작한다")
        void findByFilter_WhenFilterByCategory_ShouldReturnCategoryMatchingChallenges() {
            // given
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            GroupChallenge envChallenge = createActiveChallenge("환경 챌린지", environmentCategory, now);
            GroupChallenge healthChallenge = createActiveChallenge("건강 챌린지", healthCategory, now);
            
            // when
            List<GroupChallenge> results = queryRepository.findByFilter(null, "환경", null, null, 10);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(envChallenge.getId());
        }

        @Test
        @DisplayName("findByFilter - 검색어와 카테고리를 함께 사용한 필터링이 올바르게 동작한다")
        void findByFilter_WhenFilterByBothInputAndCategory_ShouldReturnMatchingChallenges() {
            // given
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            GroupChallenge match = createActiveChallenge("에코 라이프 챌린지", environmentCategory, now);
            GroupChallenge categoryMismatch = createActiveChallenge("에코 건강 챌린지", healthCategory, now);
            GroupChallenge inputMismatch = createActiveChallenge("플라스틱 줄이기", environmentCategory, now);
            
            // when
            List<GroupChallenge> results = queryRepository.findByFilter("에코", "환경", null, null, 10);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(match.getId());
        }

        @Test
        @DisplayName("findByFilter - 종료된 챌린지는 조회되지 않는다")
        void findByFilter_WhenChallengeEnded_ShouldExcludeFromResults() {
            // given
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            GroupChallenge activeChallenge = createActiveChallenge("활성 챌린지", environmentCategory, now);
            GroupChallenge endedChallenge = createEndedChallenge("종료된 챌린지", environmentCategory, now);
            
            // when
            List<GroupChallenge> results = queryRepository.findByFilter(null, null, null, null, 10);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(activeChallenge.getId());
        }

        @Test
        @DisplayName("findByFilter - 이벤트 챌린지는 조회되지 않는다")
        void findByFilter_WhenEventChallenge_ShouldExcludeFromResults() {
            // given
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            GroupChallenge normalChallenge = createActiveChallenge("일반 챌린지", environmentCategory, now);
            GroupChallenge eventChallenge = createEventChallenge("이벤트 챌린지", environmentCategory, now);
            
            // when
            List<GroupChallenge> results = queryRepository.findByFilter(null, null, null, null, 10);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(normalChallenge.getId());
        }

        @Test
        @DisplayName("findByFilter - 삭제된 챌린지는 조회되지 않는다")
        void findByFilter_WhenDeletedChallenge_ShouldExcludeFromResults() {
            // given
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            GroupChallenge activeChallenge = createActiveChallenge("활성 챌린지", environmentCategory, now);
            GroupChallenge deletedChallenge = createActiveChallenge("삭제된 챌린지", environmentCategory, now);
            ReflectionTestUtils.setField(deletedChallenge, "deletedAt", now);
            challengeRepository.save(deletedChallenge);
            
            // when
            List<GroupChallenge> results = queryRepository.findByFilter(null, null, null, null, 10);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(activeChallenge.getId());
        }

        @Test
        @DisplayName("findByFilter - 커서 페이지네이션이 올바르게 동작한다")
        void findByFilter_WhenUsingCursorPagination_ShouldWorkCorrectly() {
            // given
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            List<GroupChallenge> challenges = IntStream.range(0, 5)
                .mapToObj(i -> {
                    GroupChallenge challenge = createActiveChallenge("챌린지 " + i, environmentCategory, now);
                    LocalDateTime createdAt = now.minusHours(i);
                    ReflectionTestUtils.setField(challenge, "createdAt", createdAt);
                    return challenge;
                })
                .toList();
            
            em.flush();
            em.clear();
            
            // when - 첫 페이지
            List<GroupChallenge> firstPage = queryRepository.findByFilter(null, null, null, null, 2);
            
            // then
            assertThat(firstPage).hasSize(2);
            
            // when - 두 번째 페이지
            GroupChallenge lastItem = firstPage.get(firstPage.size() - 1);
            List<GroupChallenge> secondPage = queryRepository.findByFilter(
                null, null, lastItem.getId(), lastItem.getCreatedAt().toString(), 2
            );

            // then
            assertThat(secondPage).hasSize(2);
            assertThat(firstPage.get(0).getId()).isNotEqualTo(secondPage.get(0).getId());
        }

        @Test
        @DisplayName("findByFilter - 최신순(createdAt desc)으로 정렬된다")
        void findByFilter_WhenOrdering_ShouldOrderByCreatedAtDesc() {
            // given
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            
            GroupChallenge oldChallenge = createActiveChallenge("오래된 챌린지", environmentCategory, now);
            ReflectionTestUtils.setField(oldChallenge, "createdAt", now.minusDays(2));
            challengeRepository.save(oldChallenge);
            
            GroupChallenge newChallenge = createActiveChallenge("새로운 챌린지", environmentCategory, now);
            ReflectionTestUtils.setField(newChallenge, "createdAt", now);
            challengeRepository.save(newChallenge);
            
            // when
            List<GroupChallenge> results = queryRepository.findByFilter(null, null, null, null, 10);

            // then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getId()).isEqualTo(newChallenge.getId());
            assertThat(results.get(1).getId()).isEqualTo(oldChallenge.getId());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("findByFilter - 빈 검색어는 무시된다")
        void findByFilter_WhenEmptyInput_ShouldIgnore(String input) {
            // given
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            GroupChallenge challenge = createActiveChallenge("챌린지", environmentCategory, now);
            
            // when
            List<GroupChallenge> results = queryRepository.findByFilter(input, null, null, null, 10);

            // then
            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("findByFilter - 검색 결과가 없을 때 빈 리스트를 반환한다")
        void findByFilter_WhenNoResults_ShouldReturnEmptyList() {
            // given
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            createActiveChallenge("환경 챌린지", environmentCategory, now);
            
            // when
            List<GroupChallenge> results = queryRepository.findByFilter("없는검색어", null, null, null, 10);

            // then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("findByFilter - 특수문자가 포함된 검색어도 올바르게 처리한다")
        void findByFilter_WhenSpecialCharacters_ShouldHandleCorrectly() {
            // given
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            GroupChallenge challenge = createActiveChallenge("#환경 @챌린지!", environmentCategory, now);
            
            // when
            List<GroupChallenge> results = queryRepository.findByFilter("#환경", null, null, null, 10);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(challenge.getId());
        }

        @Test
        @DisplayName("findByFilter - limit이 정확히 적용된다")
        void findByFilter_WhenLimitApplied_ShouldReturnExactCount() {
            // given
            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            for (int i = 0; i < 10; i++) {
                createActiveChallenge("챌린지 " + i, environmentCategory, now);
            }
            
            // when
            List<GroupChallenge> results = queryRepository.findByFilter(null, null, null, null, 5);

            // then
            assertThat(results).hasSize(5);
        }
    }

    // Helper Methods
    private GroupChallenge createActiveChallenge(String title, GroupChallengeCategory category, LocalDateTime now) {
        GroupChallenge challenge = GroupChallengeFixture.of(testMember, category, title, false);
        ReflectionTestUtils.setField(challenge, "startDate", now.minusDays(1));
        ReflectionTestUtils.setField(challenge, "endDate", now.plusDays(7));
        return challengeRepository.save(challenge);
    }

    private GroupChallenge createActiveChallengeWithDescription(
            String title, String description, GroupChallengeCategory category, LocalDateTime now) {
        GroupChallenge challenge = GroupChallengeFixture.of(testMember, category, title, false);
        ReflectionTestUtils.setField(challenge, "description", description);
        ReflectionTestUtils.setField(challenge, "startDate", now.minusDays(1));
        ReflectionTestUtils.setField(challenge, "endDate", now.plusDays(7));
        return challengeRepository.save(challenge);
    }

    private GroupChallenge createEndedChallenge(String title, GroupChallengeCategory category, LocalDateTime now) {
        GroupChallenge challenge = GroupChallengeFixture.of(testMember, category, title, false);
        ReflectionTestUtils.setField(challenge, "startDate", now.minusDays(10));
        ReflectionTestUtils.setField(challenge, "endDate", now.minusDays(1));
        return challengeRepository.save(challenge);
    }

    private GroupChallenge createEventChallenge(String title, GroupChallengeCategory category, LocalDateTime now) {
        GroupChallenge challenge = GroupChallengeFixture.of(testMember, category, title, true);
        ReflectionTestUtils.setField(challenge, "startDate", now.minusDays(1));
        ReflectionTestUtils.setField(challenge, "endDate", now.plusDays(7));
        return challengeRepository.save(challenge);
    }
}
