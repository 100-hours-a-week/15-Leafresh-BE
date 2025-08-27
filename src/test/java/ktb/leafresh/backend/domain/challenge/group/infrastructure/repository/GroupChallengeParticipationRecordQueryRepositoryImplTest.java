package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import jakarta.persistence.EntityManager;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.query.GroupChallengeParticipationDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeParticipationCountSummaryDto;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.domain.entity.enums.TreeLevelName;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.common.entity.enums.ParticipantStatus;
import ktb.leafresh.backend.global.config.QuerydslConfig;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.support.fixture.*;
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
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * GroupChallengeParticipationRecordQueryRepositoryImpl 통합 테스트
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
@DisplayName("GroupChallengeParticipationRecordQueryRepositoryImpl 통합 테스트")
class GroupChallengeParticipationRecordQueryRepositoryImplTest {

    @Autowired
    private EntityManager em;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private GroupChallengeRepository challengeRepository;
    
    @Autowired
    private GroupChallengeParticipantRecordRepository participantRecordRepository;
    
    @Autowired
    private GroupChallengeVerificationRepository verificationRepository;
    
    private GroupChallengeParticipationRecordQueryRepositoryImpl queryRepository;
    
    private Member testMember;
    private GroupChallengeCategory testCategory;

    @BeforeEach
    void setUp() {
        queryRepository = new GroupChallengeParticipationRecordQueryRepositoryImpl(
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
        
        testCategory = GroupChallengeCategoryFixture.of("환경보호");
        em.persist(testCategory);
        
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("countParticipationByStatus - 참여 중인 챌린지가 없을 때 모든 카운트가 0을 반환한다")
    void countParticipationByStatus_WhenNoParticipation_ShouldReturnZeroCounts() {
        // when
        GroupChallengeParticipationCountSummaryDto result = 
            queryRepository.countParticipationByStatus(testMember.getId());

        // then
        assertThat(result.notStarted()).isZero();
        assertThat(result.ongoing()).isZero();
        assertThat(result.completed()).isZero();
    }

    @Test
    @DisplayName("countParticipationByStatus - 시작하지 않은 챌린지를 정확히 카운트한다")
    void countParticipationByStatus_WhenNotStarted_ShouldCountCorrectly() {
        // given
        GroupChallenge challenge = createOngoingChallenge();
        GroupChallengeParticipantRecord record = createParticipantRecord(challenge, testMember);
        
        // when
        GroupChallengeParticipationCountSummaryDto result = 
            queryRepository.countParticipationByStatus(testMember.getId());

        // then
        assertThat(result.notStarted()).isEqualTo(1);
        assertThat(result.ongoing()).isZero();
        assertThat(result.completed()).isZero();
    }

    @Test
    @DisplayName("countParticipationByStatus - 진행 중인 챌린지를 정확히 카운트한다")
    void countParticipationByStatus_WhenOngoing_ShouldCountCorrectly() {
        // given
        GroupChallenge challenge = createOngoingChallenge();
        GroupChallengeParticipantRecord record = createParticipantRecord(challenge, testMember);
        createVerification(record, ChallengeStatus.SUCCESS);
        
        // when
        GroupChallengeParticipationCountSummaryDto result = 
            queryRepository.countParticipationByStatus(testMember.getId());

        // then
        assertThat(result.notStarted()).isZero();
        assertThat(result.ongoing()).isEqualTo(1);
        assertThat(result.completed()).isZero();
    }

    @Test
    @DisplayName("countParticipationByStatus - 완료된 챌린지를 정확히 카운트한다")
    void countParticipationByStatus_WhenCompleted_ShouldCountCorrectly() {
        // given
        GroupChallenge challenge = createCompletedChallenge();
        createParticipantRecord(challenge, testMember);
        
        // when
        GroupChallengeParticipationCountSummaryDto result = 
            queryRepository.countParticipationByStatus(testMember.getId());

        // then
        assertThat(result.notStarted()).isZero();
        assertThat(result.ongoing()).isZero();
        assertThat(result.completed()).isEqualTo(1);
    }

    @Test
    @DisplayName("countParticipationByStatus - FINISHED 상태는 completed로 카운트된다")
    void countParticipationByStatus_WhenFinishedStatus_ShouldCountAsCompleted() {
        // given
        GroupChallenge challenge = createOngoingChallenge();
        createParticipantRecord(challenge, testMember, ParticipantStatus.FINISHED);
        
        // when
        GroupChallengeParticipationCountSummaryDto result = 
            queryRepository.countParticipationByStatus(testMember.getId());

        // then
        assertThat(result.completed()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"DROPPED", "BANNED"})
    @DisplayName("countParticipationByStatus - 비활성 상태는 카운트에서 제외된다")
    void countParticipationByStatus_WhenInactiveStatus_ShouldExcludeFromCount(String status) {
        // given
        GroupChallenge challenge = createOngoingChallenge();
        createParticipantRecord(challenge, testMember, ParticipantStatus.valueOf(status));
        
        // when
        GroupChallengeParticipationCountSummaryDto result = 
            queryRepository.countParticipationByStatus(testMember.getId());

        // then
        assertThat(result.notStarted()).isZero();
        assertThat(result.ongoing()).isZero();
        assertThat(result.completed()).isZero();
    }

    @Test
    @DisplayName("countParticipationByStatus - 삭제된 레코드는 카운트에서 제외된다")
    void countParticipationByStatus_WhenDeletedRecords_ShouldExcludeFromCount() {
        // given
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        
        // 삭제된 챌린지
        GroupChallenge deletedChallenge = createOngoingChallenge();
        ReflectionTestUtils.setField(deletedChallenge, "deletedAt", now);
        challengeRepository.save(deletedChallenge);
        createParticipantRecord(deletedChallenge, testMember);
        
        // 삭제된 참가 기록
        GroupChallenge normalChallenge = createOngoingChallenge();
        GroupChallengeParticipantRecord deletedRecord = createParticipantRecord(normalChallenge, testMember);
        ReflectionTestUtils.setField(deletedRecord, "deletedAt", now);
        participantRecordRepository.save(deletedRecord);
        
        // when
        GroupChallengeParticipationCountSummaryDto result = 
            queryRepository.countParticipationByStatus(testMember.getId());

        // then
        assertThat(result.notStarted()).isZero();
        assertThat(result.ongoing()).isZero();
        assertThat(result.completed()).isZero();
    }

    @Test
    @DisplayName("findParticipatedByStatus - 지정한 상태의 챌린지만 조회된다")
    void findParticipatedByStatus_WhenSearchingByStatus_ShouldReturnOnlyMatchingStatus() {
        // given
        GroupChallenge notStartedChallenge = createOngoingChallenge();
        createParticipantRecord(notStartedChallenge, testMember);
        
        GroupChallenge ongoingChallenge = createOngoingChallenge();
        GroupChallengeParticipantRecord ongoingRecord = createParticipantRecord(ongoingChallenge, testMember);
        createVerification(ongoingRecord, ChallengeStatus.SUCCESS);
        
        GroupChallenge completedChallenge = createCompletedChallenge();
        createParticipantRecord(completedChallenge, testMember);
        
        // when
        List<GroupChallengeParticipationDto> notStartedResults = 
            queryRepository.findParticipatedByStatus(testMember.getId(), "not_started", null, null, 10);
        List<GroupChallengeParticipationDto> ongoingResults = 
            queryRepository.findParticipatedByStatus(testMember.getId(), "ongoing", null, null, 10);
        List<GroupChallengeParticipationDto> completedResults = 
            queryRepository.findParticipatedByStatus(testMember.getId(), "completed", null, null, 10);

        // then
        assertThat(notStartedResults).hasSize(1);
        assertThat(notStartedResults.get(0).getId()).isEqualTo(notStartedChallenge.getId());
        
        assertThat(ongoingResults).hasSize(1);
        assertThat(ongoingResults.get(0).getId()).isEqualTo(ongoingChallenge.getId());
        
        assertThat(completedResults).hasSize(1);
        assertThat(completedResults.get(0).getId()).isEqualTo(completedChallenge.getId());
    }

    @Test
    @DisplayName("findParticipatedByStatus - 성공 인증 수가 정확히 계산된다")
    void findParticipatedByStatus_WhenCalculatingSuccessCount_ShouldBeAccurate() {
        // given
        GroupChallenge challenge = createOngoingChallenge();
        GroupChallengeParticipantRecord record = createParticipantRecord(challenge, testMember);
        
        // 성공 2개
        createVerification(record, ChallengeStatus.SUCCESS);
        createVerification(record, ChallengeStatus.SUCCESS);
        // 실패 1개
        createVerification(record, ChallengeStatus.FAILURE);
        // 대기 1개
        createVerification(record, ChallengeStatus.PENDING_APPROVAL);
        
        // when
        List<GroupChallengeParticipationDto> results = 
            queryRepository.findParticipatedByStatus(testMember.getId(), "ongoing", null, null, 10);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSuccess()).isEqualTo(2L);
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid_status", "INVALID", "", "unknown"})
    @DisplayName("findParticipatedByStatus - 유효하지 않은 상태값으로 조회 시 예외가 발생한다")
    void findParticipatedByStatus_WhenInvalidStatus_ShouldThrowException(String invalidStatus) {
        // when & then
        assertThatThrownBy(() -> 
            queryRepository.findParticipatedByStatus(testMember.getId(), invalidStatus, null, null, 10)
        ).isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("findParticipatedByStatus - 커서 페이지네이션이 올바르게 동작한다")
    void findParticipatedByStatus_WhenUsingCursorPagination_ShouldWorkCorrectly() {
        // given
        List<GroupChallenge> challenges = IntStream.range(0, 5)
            .mapToObj(i -> {
                GroupChallenge challenge = createOngoingChallenge();
                LocalDateTime createdAt = LocalDateTime.now(ZoneOffset.UTC).minusHours(i);
                ReflectionTestUtils.setField(challenge, "createdAt", createdAt);
                challengeRepository.save(challenge);
                createParticipantRecord(challenge, testMember);
                return challenge;
            })
            .toList();
        
        // when - 첫 페이지
        List<GroupChallengeParticipationDto> firstPage = 
            queryRepository.findParticipatedByStatus(testMember.getId(), "not_started", null, null, 2);
        
        // then
        assertThat(firstPage).hasSizeGreaterThanOrEqualTo(2);
        
        // when - 두 번째 페이지
        if (firstPage.size() > 1) {
            GroupChallengeParticipationDto lastOfFirstPage = firstPage.get(1);
            List<GroupChallengeParticipationDto> secondPage = 
                queryRepository.findParticipatedByStatus(
                    testMember.getId(), 
                    "not_started", 
                    lastOfFirstPage.getId(), 
                    lastOfFirstPage.getCreatedAt().toString(), 
                    2
                );
            
            // then
            assertThat(secondPage).isNotEmpty();
            assertThat(secondPage.get(0).getId()).isNotEqualTo(firstPage.get(0).getId());
        }
    }

    @Test
    @DisplayName("findParticipatedByStatus - 최신순으로 정렬된다")
    void findParticipatedByStatus_WhenOrdering_ShouldOrderByCreatedAtDesc() {
        // given
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        
        GroupChallenge oldChallenge = createOngoingChallenge();
        ReflectionTestUtils.setField(oldChallenge, "createdAt", now.minusDays(2));
        challengeRepository.save(oldChallenge);
        createParticipantRecord(oldChallenge, testMember);
        
        GroupChallenge newChallenge = createOngoingChallenge();
        ReflectionTestUtils.setField(newChallenge, "createdAt", now);
        challengeRepository.save(newChallenge);
        createParticipantRecord(newChallenge, testMember);
        
        // when
        List<GroupChallengeParticipationDto> results = 
            queryRepository.findParticipatedByStatus(testMember.getId(), "not_started", null, null, 10);

        // then
        assertThat(results).hasSizeGreaterThanOrEqualTo(2);
        assertThat(results.get(0).getId()).isEqualTo(newChallenge.getId());
        assertThat(results.get(1).getId()).isEqualTo(oldChallenge.getId());
    }

    // Helper Methods
    private GroupChallenge createOngoingChallenge() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        GroupChallenge challenge = GroupChallengeFixture.of(testMember, testCategory);
        ReflectionTestUtils.setField(challenge, "startDate", now.minusDays(1));
        ReflectionTestUtils.setField(challenge, "endDate", now.plusDays(5));
        return challengeRepository.save(challenge);
    }

    private GroupChallenge createCompletedChallenge() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        GroupChallenge challenge = GroupChallengeFixture.of(testMember, testCategory);
        ReflectionTestUtils.setField(challenge, "startDate", now.minusDays(10));
        ReflectionTestUtils.setField(challenge, "endDate", now.minusDays(1));
        return challengeRepository.save(challenge);
    }

    private GroupChallengeParticipantRecord createParticipantRecord(
            GroupChallenge challenge, Member member) {
        return createParticipantRecord(challenge, member, ParticipantStatus.ACTIVE);
    }

    private GroupChallengeParticipantRecord createParticipantRecord(
            GroupChallenge challenge, Member member, ParticipantStatus status) {
        GroupChallengeParticipantRecord record = 
            GroupChallengeParticipantRecordFixture.of(challenge, member, status);
        return participantRecordRepository.save(record);
    }

    private GroupChallengeVerification createVerification(
            GroupChallengeParticipantRecord record, ChallengeStatus status) {
        GroupChallengeVerification verification = 
            GroupChallengeVerificationFixture.of(record, status);
        return verificationRepository.save(verification);
    }
}
