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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * GroupChallengeCreatedQueryRepositoryImpl 통합 테스트
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
@DisplayName("GroupChallengeCreatedQueryRepositoryImpl 통합 테스트")
class GroupChallengeCreatedQueryRepositoryImplTest {

    @Autowired
    private EntityManager em;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private GroupChallengeRepository challengeRepository;
    
    private GroupChallengeCreatedQueryRepositoryImpl queryRepository;
    
    private Member testCreator;
    private Member otherMember;
    private GroupChallengeCategory testCategory;

    @BeforeEach
    void setUp() {
        queryRepository = new GroupChallengeCreatedQueryRepositoryImpl(
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
        
        testCreator = MemberFixture.of("creator@example.com", "챌린지 생성자");
        ReflectionTestUtils.setField(testCreator, "treeLevel", treeLevel);
        testCreator = memberRepository.save(testCreator);
        
        otherMember = MemberFixture.of("other@example.com", "다른 회원");
        ReflectionTestUtils.setField(otherMember, "treeLevel", treeLevel);
        otherMember = memberRepository.save(otherMember);
        
        testCategory = GroupChallengeCategoryFixture.of("환경보호");
        em.persist(testCategory);
        
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("findCreatedByMember - 생성한 챌린지가 없을 때 빈 리스트를 반환한다")
    void findCreatedByMember_WhenNoCreatedChallenges_ShouldReturnEmptyList() {
        // given
        // 다른 사람이 만든 챌린지만 존재
        createChallenge(otherMember, "다른 사람 챌린지");

        // when
        List<GroupChallenge> results = 
            queryRepository.findCreatedByMember(testCreator.getId(), null, null, 10);

        // then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("findCreatedByMember - 특정 회원이 생성한 챌린지만 조회된다")
    void findCreatedByMember_WhenMemberCreatedChallenges_ShouldReturnOnlyTheirChallenges() {
        // given
        GroupChallenge creatorChallenge1 = createChallenge(testCreator, "내가 만든 챌린지 1");
        GroupChallenge creatorChallenge2 = createChallenge(testCreator, "내가 만든 챌린지 2");
        GroupChallenge otherChallenge = createChallenge(otherMember, "다른 사람 챌린지");

        // when
        List<GroupChallenge> results = 
            queryRepository.findCreatedByMember(testCreator.getId(), null, null, 10);

        // then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(GroupChallenge::getId)
            .containsExactlyInAnyOrder(creatorChallenge1.getId(), creatorChallenge2.getId());
        assertThat(results).extracting(GroupChallenge::getTitle)
            .containsExactlyInAnyOrder("내가 만든 챌린지 1", "내가 만든 챌린지 2");
    }

    @Test
    @DisplayName("findCreatedByMember - 삭제된 챌린지는 조회되지 않는다")
    void findCreatedByMember_WhenChallengeDeleted_ShouldExcludeFromResults() {
        // given
        LocalDateTime now = LocalDateTime.now();
        
        GroupChallenge activeChallenge = createChallenge(testCreator, "활성 챌린지");
        
        GroupChallenge deletedChallenge = createChallenge(testCreator, "삭제된 챌린지");
        ReflectionTestUtils.setField(deletedChallenge, "deletedAt", now);
        challengeRepository.save(deletedChallenge);

        // when
        List<GroupChallenge> results = 
            queryRepository.findCreatedByMember(testCreator.getId(), null, null, 10);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(activeChallenge.getId());
        assertThat(results.get(0).getTitle()).isEqualTo("활성 챌린지");
    }

    @Test
    @DisplayName("findCreatedByMember - 최신순(createdAt desc, id desc)으로 정렬된다")
    void findCreatedByMember_WhenOrdering_ShouldOrderByCreatedAtDescIdDesc() {
        // given
        LocalDateTime now = LocalDateTime.now();
        
        GroupChallenge challenge1 = createChallenge(testCreator, "첫 번째 챌린지");
        GroupChallenge challenge2 = createChallenge(testCreator, "두 번째 챌린지");
        GroupChallenge challenge3 = createChallenge(testCreator, "세 번째 챌린지");
        
        // 네이티브 쿼리로 createdAt 직접 업데이트
        em.createNativeQuery("UPDATE group_challenges SET created_at = ? WHERE id = ?")
            .setParameter(1, now.minusDays(2))
            .setParameter(2, challenge1.getId())
            .executeUpdate();
            
        em.createNativeQuery("UPDATE group_challenges SET created_at = ? WHERE id = ?")
            .setParameter(1, now)
            .setParameter(2, challenge2.getId())
            .executeUpdate();
            
        em.createNativeQuery("UPDATE group_challenges SET created_at = ? WHERE id = ?")
            .setParameter(1, now.minusDays(1))
            .setParameter(2, challenge3.getId())
            .executeUpdate();

        em.flush();
        em.clear();

        // when
        List<GroupChallenge> results = 
            queryRepository.findCreatedByMember(testCreator.getId(), null, null, 10);

        // then
        assertThat(results).hasSize(3);
        
        // 제목으로 정렬 순서 검증 (최신순 = DESC)
        assertThat(results.get(0).getTitle()).isEqualTo("두 번째 챌린지");  // now
        assertThat(results.get(1).getTitle()).isEqualTo("세 번째 챌린지");  // now.minusDays(1)
        assertThat(results.get(2).getTitle()).isEqualTo("첫 번째 챌린지");  // now.minusDays(2)
        
        // createdAt 상대적 순서 검증
        assertThat(results.get(0).getCreatedAt()).isAfterOrEqualTo(results.get(1).getCreatedAt());
        assertThat(results.get(1).getCreatedAt()).isAfterOrEqualTo(results.get(2).getCreatedAt());
    }

    @Test
    @DisplayName("findCreatedByMember - 커서 페이지네이션이 올바르게 동작한다")
    void findCreatedByMember_WhenUsingCursorPagination_ShouldWorkCorrectly() {
        // given - 시간 차이를 두고 생성
        LocalDateTime baseTime = LocalDateTime.now();
        List<GroupChallenge> challenges = IntStream.range(0, 5)
            .mapToObj(i -> createChallenge(testCreator, "챌린지 " + i))
            .toList();
        
        // 네이티브 쿼리로 각각의 시간을 설정
        for (int i = 0; i < challenges.size(); i++) {
            GroupChallenge challenge = challenges.get(i);
            LocalDateTime createdAt = baseTime.minusHours(i);  // 역순으로 시간 설정
            em.createNativeQuery("UPDATE group_challenges SET created_at = ? WHERE id = ?")
                .setParameter(1, createdAt)
                .setParameter(2, challenge.getId())
                .executeUpdate();
        }

        em.flush();
        em.clear();

        // when - 첫 페이지 (최신 2개)
        List<GroupChallenge> firstPage = 
            queryRepository.findCreatedByMember(testCreator.getId(), null, null, 2);
        
        // then
        assertThat(firstPage).hasSize(2);
        
        // when - 두 번째 페이지 (다음 2개)
        GroupChallenge lastOfFirstPage = firstPage.get(1);
        List<GroupChallenge> secondPage = 
            queryRepository.findCreatedByMember(
                testCreator.getId(), 
                lastOfFirstPage.getId(), 
                lastOfFirstPage.getCreatedAt().toString(), 
                2
            );
        
        // then
        assertThat(secondPage).hasSize(2);
        // 첫 번째 페이지와 다른 데이터여야 함
        assertThat(secondPage).extracting(GroupChallenge::getId)
            .doesNotContain(firstPage.get(0).getId(), firstPage.get(1).getId());
    }

    @Test
    @DisplayName("findCreatedByMember - limit 파라미터가 올바르게 적용된다")
    void findCreatedByMember_WhenLimitProvided_ShouldRespectLimit() {
        // given
        IntStream.range(0, 10)
            .forEach(i -> createChallenge(testCreator, "챌린지 " + i));

        // when
        List<GroupChallenge> results = 
            queryRepository.findCreatedByMember(testCreator.getId(), null, null, 3);

        // then
        assertThat(results).hasSize(3);
    }

    @Test
    @DisplayName("findCreatedByMember - 커서 페이지네이션 시 중복 데이터가 없다")
    void findCreatedByMember_WhenPaginating_ShouldNotHaveDuplicates() {
        // given - 충분한 시간 간격을 두고 데이터 생성
        LocalDateTime baseTime = LocalDateTime.now();
        List<GroupChallenge> challenges = IntStream.range(0, 10)
            .mapToObj(i -> createChallenge(testCreator, "챌린지 " + i))
            .toList();
        
        // 네이티브 쿼리로 시간 설정 (5분 간격)
        for (int i = 0; i < challenges.size(); i++) {
            GroupChallenge challenge = challenges.get(i);
            LocalDateTime createdAt = baseTime.minusMinutes(i * 5);  // 5분 간격
            em.createNativeQuery("UPDATE group_challenges SET created_at = ? WHERE id = ?")
                .setParameter(1, createdAt)
                .setParameter(2, challenge.getId())
                .executeUpdate();
        }

        em.flush();
        em.clear();

        // when - 모든 페이지 조회
        Set<Long> allIds = new HashSet<>();
        Long cursorId = null;
        String cursorTimestamp = null;
        int totalFetched = 0;
        
        for (int i = 0; i < 5; i++) {  // 최대 5번 페이징
            List<GroupChallenge> page = queryRepository.findCreatedByMember(
                testCreator.getId(), cursorId, cursorTimestamp, 2
            );
            
            if (page.isEmpty()) break;
            
            page.forEach(challenge -> {
                boolean wasNew = allIds.add(challenge.getId());
                assertThat(wasNew).isTrue();  // 중복이 있다면 false
            });
            
            totalFetched += page.size();
            
            GroupChallenge lastItem = page.get(page.size() - 1);
            cursorId = lastItem.getId();
            cursorTimestamp = lastItem.getCreatedAt().toString();
        }

        // then
        assertThat(allIds).hasSize(10); // 모든 데이터가 중복 없이 조회되어야 함
        assertThat(totalFetched).isEqualTo(10);
    }

    @Test
    @DisplayName("findCreatedByMember - 동일한 createdAt을 가진 챌린지들도 올바르게 페이지네이션된다")
    void findCreatedByMember_WhenSameCreatedAt_ShouldPaginateCorrectlyById() {
        // given - 동일한 시간으로 5개 생성
        LocalDateTime sameTime = LocalDateTime.now();
        List<GroupChallenge> challenges = IntStream.range(0, 5)
            .mapToObj(i -> createChallenge(testCreator, "동시 생성 챌린지 " + i))
            .toList();
        
        // 모든 챌린지에 동일한 시간 설정
        for (GroupChallenge challenge : challenges) {
            em.createNativeQuery("UPDATE group_challenges SET created_at = ? WHERE id = ?")
                .setParameter(1, sameTime)
                .setParameter(2, challenge.getId())
                .executeUpdate();
        }

        em.flush();
        em.clear();

        // when - 첫 번째 페이지 (2개)
        List<GroupChallenge> firstPage = 
            queryRepository.findCreatedByMember(testCreator.getId(), null, null, 2);
        
        // then
        assertThat(firstPage).hasSize(2);
        
        // when - 두 번째 페이지 (나머지 3개)
        GroupChallenge lastItem = firstPage.get(firstPage.size() - 1);
        List<GroupChallenge> secondPage = 
            queryRepository.findCreatedByMember(
                testCreator.getId(), 
                lastItem.getId(), 
                lastItem.getCreatedAt().toString(), 
                3
            );

        // then
        assertThat(secondPage).hasSize(3);
        
        // 중복 검증 - 교집합이 없어야 함
        Set<Long> firstPageIds = firstPage.stream()
            .map(GroupChallenge::getId)
            .collect(java.util.stream.Collectors.toSet());
        Set<Long> secondPageIds = secondPage.stream()
            .map(GroupChallenge::getId)
            .collect(java.util.stream.Collectors.toSet());
            
        Set<Long> intersection = new HashSet<>(firstPageIds);
        intersection.retainAll(secondPageIds);
        assertThat(intersection).isEmpty();
        assertThat(firstPageIds.size() + secondPageIds.size()).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 20, 50})
    @DisplayName("findCreatedByMember - 다양한 limit 값에 대해 올바르게 동작한다")
    void findCreatedByMember_WhenVariousLimits_ShouldWorkCorrectly(int limit) {
        // given
        int totalChallenges = 30;
        IntStream.range(0, totalChallenges)
            .forEach(i -> createChallenge(testCreator, "챌린지 " + i));

        // when
        List<GroupChallenge> results = 
            queryRepository.findCreatedByMember(testCreator.getId(), null, null, limit);

        // then
        int expectedSize = Math.min(limit, totalChallenges);
        assertThat(results).hasSizeLessThanOrEqualTo(expectedSize);
        if (limit <= totalChallenges) {
            assertThat(results).hasSize(limit);
        }
    }

    @Test
    @DisplayName("findCreatedByMember - 빈 커서로 시작할 때 첫 페이지를 반환한다")
    void findCreatedByMember_WhenNullCursor_ShouldReturnFirstPage() {
        // given
        IntStream.range(0, 5)
            .forEach(i -> createChallenge(testCreator, "챌린지 " + i));

        // when
        List<GroupChallenge> results = 
            queryRepository.findCreatedByMember(testCreator.getId(), null, null, 10);

        // then
        assertThat(results).hasSize(5);
    }

    @Test
    @DisplayName("findCreatedByMember - 마지막 페이지 이후 조회 시 빈 리스트를 반환한다")
    void findCreatedByMember_WhenBeyondLastPage_ShouldReturnEmptyList() {
        // given
        GroupChallenge onlyChallenge = createChallenge(testCreator, "유일한 챌린지");

        // when - 첫 페이지
        List<GroupChallenge> firstPage = 
            queryRepository.findCreatedByMember(testCreator.getId(), null, null, 10);
        
        // when - 마지막 아이템 이후 조회
        GroupChallenge lastItem = firstPage.get(0);
        List<GroupChallenge> nextPage = 
            queryRepository.findCreatedByMember(
                testCreator.getId(), 
                lastItem.getId(), 
                lastItem.getCreatedAt().toString(), 
                10
            );

        // then
        assertThat(firstPage).hasSize(1);
        assertThat(nextPage).isEmpty();
    }

    @Test
    @DisplayName("findCreatedByMember - 엔티티의 모든 필드가 올바르게 조회된다")
    void findCreatedByMember_WhenMapping_ShouldLoadAllFieldsCorrectly() {
        // given
        LocalDateTime now = LocalDateTime.now();
        GroupChallenge challenge = GroupChallengeFixture.of(testCreator, testCategory);
        ReflectionTestUtils.setField(challenge, "title", "테스트 챌린지");
        ReflectionTestUtils.setField(challenge, "description", "테스트 설명");
        ReflectionTestUtils.setField(challenge, "startDate", now.plusDays(1));
        ReflectionTestUtils.setField(challenge, "endDate", now.plusDays(7));
        ReflectionTestUtils.setField(challenge, "maxParticipantCount", 10);
        ReflectionTestUtils.setField(challenge, "eventFlag", true);
        challenge = challengeRepository.save(challenge);
        
        // 저장 후 실제 createdAt 값 확인
        em.flush();
        em.clear();
        
        // 실제 저장된 챌린지 다시 조회하여 createdAt 확인
        GroupChallenge savedChallenge = challengeRepository.findById(challenge.getId()).orElseThrow();
        LocalDateTime actualCreatedAt = savedChallenge.getCreatedAt();

        // when
        List<GroupChallenge> results = 
            queryRepository.findCreatedByMember(testCreator.getId(), null, null, 10);

        // then
        assertThat(results).hasSize(1);
        GroupChallenge result = results.get(0);
        assertThat(result.getId()).isEqualTo(challenge.getId());
        assertThat(result.getTitle()).isEqualTo("테스트 챌린지");
        assertThat(result.getDescription()).isEqualTo("테스트 설명");
        assertThat(result.getStartDate()).isEqualTo(now.plusDays(1));
        assertThat(result.getEndDate()).isEqualTo(now.plusDays(7));
        assertThat(result.getMaxParticipantCount()).isEqualTo(10);
        assertThat(result.getEventFlag()).isTrue();
        assertThat(result.getCategory().getName()).isEqualTo("환경보호");
        assertThat(result.getCreatedAt()).isEqualTo(actualCreatedAt);
        assertThat(result.getMember().getId()).isEqualTo(testCreator.getId());
    }

    @Test
    @DisplayName("findCreatedByMember - 커서 조건 유틸리티가 올바르게 적용된다")
    void findCreatedByMember_WhenUsingCursorCondition_ShouldApplyConditionCorrectly() {
        // given - 시간 순서대로 3개 생성
        LocalDateTime baseTime = LocalDateTime.now();
        
        GroupChallenge challenge1 = createChallenge(testCreator, "챌린지 1");
        GroupChallenge challenge2 = createChallenge(testCreator, "챌린지 2");
        GroupChallenge challenge3 = createChallenge(testCreator, "챌린지 3");
        
        // 네이티브 쿼리로 createdAt 직접 설정
        em.createNativeQuery("UPDATE group_challenges SET created_at = ? WHERE id = ?")
            .setParameter(1, baseTime.minusHours(3))  // 가장 오래된
            .setParameter(2, challenge1.getId())
            .executeUpdate();
            
        em.createNativeQuery("UPDATE group_challenges SET created_at = ? WHERE id = ?")
            .setParameter(1, baseTime.minusHours(2))  // 중간
            .setParameter(2, challenge2.getId())
            .executeUpdate();
            
        em.createNativeQuery("UPDATE group_challenges SET created_at = ? WHERE id = ?")
            .setParameter(1, baseTime.minusHours(1))  // 가장 최신
            .setParameter(2, challenge3.getId())
            .executeUpdate();

        em.flush();
        em.clear();
        
        // 실제 저장된 값들을 다시 조회해서 확인
        challenge1 = challengeRepository.findById(challenge1.getId()).orElseThrow();
        challenge2 = challengeRepository.findById(challenge2.getId()).orElseThrow();
        challenge3 = challengeRepository.findById(challenge3.getId()).orElseThrow();

        // when - challenge3(가장 최신)을 커서로 사용하여 그보다 이전 데이터 조회
        List<GroupChallenge> results = queryRepository.findCreatedByMember(
            testCreator.getId(), 
            challenge3.getId(), 
            challenge3.getCreatedAt().toString(),  // 실제 저장된 createdAt 사용
            10
        );

        // then - challenge3보다 이전인 challenge2, challenge1만 조회되어야 함
        assertThat(results).hasSize(2);
        assertThat(results).extracting(GroupChallenge::getTitle)
            .containsExactly("챌린지 2", "챌린지 1");  // DESC 정렬이므로 challenge2가 먼저
    }

    // Helper Methods
    private GroupChallenge createChallenge(Member creator, String title) {
        GroupChallenge challenge = GroupChallengeFixture.of(creator, testCategory);
        ReflectionTestUtils.setField(challenge, "title", title);
        return challengeRepository.save(challenge);
    }
}
