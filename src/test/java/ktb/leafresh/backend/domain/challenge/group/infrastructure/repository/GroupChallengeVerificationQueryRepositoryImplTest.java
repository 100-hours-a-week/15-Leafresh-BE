package ktb.leafresh.backend.domain.challenge.group.infrastructure.repository;

import jakarta.persistence.EntityManager;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeParticipationSummaryDto;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.domain.entity.enums.TreeLevelName;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.support.config.RepositoryTestSupport;
import ktb.leafresh.backend.support.fixture.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * GroupChallengeVerificationQueryRepositoryImpl 통합 테스트
 * 
 * 빅테크 기업의 테스트 Best Practice를 따름:
 * - 명확한 테스트 이름 (Given-When-Then 패턴)
 * - 독립적인 테스트 데이터 설정
 * - Repository를 활용한 실제 DB 테스트
 * - 엣지 케이스 포함
 * - Nested 클래스를 통한 테스트 구조화
 */
@RepositoryTestSupport
@DisplayName("GroupChallengeVerificationQueryRepositoryImpl 통합 테스트")
class GroupChallengeVerificationQueryRepositoryImplTest {

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

    private GroupChallengeVerificationQueryRepositoryImpl queryRepository;

    private Member testMember;
    private GroupChallengeCategory testCategory;
    private GroupChallenge testChallenge;
    private GroupChallengeParticipantRecord testParticipantRecord;

    @BeforeEach
    void setUp() {
        queryRepository = new GroupChallengeVerificationQueryRepositoryImpl(
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
        
        testChallenge = GroupChallengeFixture.of(testMember, testCategory);
        testChallenge = challengeRepository.save(testChallenge);
        
        testParticipantRecord = GroupChallengeParticipantRecordFixture.of(testChallenge, testMember);
        testParticipantRecord = participantRecordRepository.save(testParticipantRecord);
        
        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("findByChallengeId 메서드")
    class FindByChallengeId {

        @Test
        @DisplayName("특정 챌린지의 모든 인증 기록을 조회해야 한다")
        void shouldFindAllVerificationsForChallenge() {
            // given
            GroupChallengeVerification verification1 = 
                GroupChallengeVerificationFixture.of(testParticipantRecord);
            GroupChallengeVerification verification2 = 
                GroupChallengeVerificationFixture.of(testParticipantRecord);
            verificationRepository.save(verification1);
            verificationRepository.save(verification2);
            
            // 다른 챌린지의 인증
            Member otherMember = MemberFixture.of("other@example.com", "다른사용자");
            ReflectionTestUtils.setField(otherMember, "treeLevel", testMember.getTreeLevel());
            otherMember = memberRepository.save(otherMember);
            
            GroupChallenge otherChallenge = GroupChallengeFixture.of(otherMember, testCategory);
            otherChallenge = challengeRepository.save(otherChallenge);
            
            GroupChallengeParticipantRecord otherRecord = 
                GroupChallengeParticipantRecordFixture.of(otherChallenge, otherMember);
            otherRecord = participantRecordRepository.save(otherRecord);
            
            GroupChallengeVerification otherVerification = 
                GroupChallengeVerificationFixture.of(otherRecord);
            verificationRepository.save(otherVerification);
            
            em.flush();
            em.clear();

            // when
            List<GroupChallengeVerification> results = 
                queryRepository.findByChallengeId(testChallenge.getId(), null, null, 10);

            // then
            assertThat(results).hasSize(2);
            assertThat(results).extracting("id")
                .containsExactlyInAnyOrder(verification1.getId(), verification2.getId());
        }

        @Test
        @DisplayName("커서 페이지네이션이 올바르게 동작해야 한다")
        void shouldPaginateCorrectlyWithCursor() {
            // given
            LocalDateTime baseTime = LocalDateTime.of(2024, 1, 1, 12, 0);
            List<GroupChallengeVerification> verifications = createMultipleVerifications(5, baseTime);
            
            em.flush();
            em.clear();

            // when - 첫 페이지
            List<GroupChallengeVerification> firstPage = 
                queryRepository.findByChallengeId(testChallenge.getId(), null, null, 2);
            
            // when - 두 번째 페이지
            GroupChallengeVerification lastItem = firstPage.get(firstPage.size() - 1);
            List<GroupChallengeVerification> secondPage = 
                queryRepository.findByChallengeId(
                    testChallenge.getId(), 
                    lastItem.getId(), 
                    lastItem.getCreatedAt().toString(), 
                    2
                );

            // then
            assertThat(firstPage).hasSize(2);
            assertThat(secondPage).hasSize(2);
            assertThat(firstPage.get(0).getId()).isNotEqualTo(secondPage.get(0).getId());
        }

        @Test
        @DisplayName("삭제된 인증은 조회되지 않아야 한다")
        void shouldNotFindDeletedVerifications() {
            // given
            LocalDateTime now = LocalDateTime.now();
            
            GroupChallengeVerification activeVerification = 
                GroupChallengeVerificationFixture.of(testParticipantRecord);
            verificationRepository.save(activeVerification);
            
            GroupChallengeVerification deletedVerification = 
                GroupChallengeVerificationFixture.of(testParticipantRecord);
            ReflectionTestUtils.setField(deletedVerification, "deletedAt", now);
            verificationRepository.save(deletedVerification);
            
            em.flush();
            em.clear();

            // when
            List<GroupChallengeVerification> results = 
                queryRepository.findByChallengeId(testChallenge.getId(), null, null, 10);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(activeVerification.getId());
        }

        @Test
        @DisplayName("최신순(createdAt desc)으로 정렬되어야 한다")
        void shouldOrderByCreatedAtDesc() {
            // given
            LocalDateTime now = LocalDateTime.now();
            
            GroupChallengeVerification oldVerification = 
                GroupChallengeVerificationFixture.of(testParticipantRecord);
            ReflectionTestUtils.setField(oldVerification, "createdAt", now.minusDays(2));
            verificationRepository.save(oldVerification);
            
            GroupChallengeVerification newVerification = 
                GroupChallengeVerificationFixture.of(testParticipantRecord);
            ReflectionTestUtils.setField(newVerification, "createdAt", now);
            verificationRepository.save(newVerification);
            
            em.flush();
            em.clear();

            // when
            List<GroupChallengeVerification> results = 
                queryRepository.findByChallengeId(testChallenge.getId(), null, null, 10);

            // then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getId()).isEqualTo(newVerification.getId());
            assertThat(results.get(1).getId()).isEqualTo(oldVerification.getId());
        }
    }

    @Nested
    @DisplayName("findByParticipantRecordId 메서드")
    class FindByParticipantRecordId {

        @Test
        @DisplayName("특정 참가자의 모든 인증 기록을 조회해야 한다")
        void shouldFindAllVerificationsForParticipant() {
            // given
            GroupChallengeVerification verification1 = 
                GroupChallengeVerificationFixture.of(testParticipantRecord, ChallengeStatus.SUCCESS);
            GroupChallengeVerification verification2 = 
                GroupChallengeVerificationFixture.of(testParticipantRecord, ChallengeStatus.FAILURE);
            GroupChallengeVerification verification3 = 
                GroupChallengeVerificationFixture.of(testParticipantRecord, ChallengeStatus.PENDING_APPROVAL);
            
            verificationRepository.save(verification1);
            verificationRepository.save(verification2);
            verificationRepository.save(verification3);
            
            em.flush();
            em.clear();

            // when
            List<GroupChallengeVerification> results = 
                queryRepository.findByParticipantRecordId(testParticipantRecord.getId());

            // then
            assertThat(results).hasSize(3);
            assertThat(results).extracting("status")
                .containsExactlyInAnyOrder(
                    ChallengeStatus.SUCCESS, 
                    ChallengeStatus.FAILURE, 
                    ChallengeStatus.PENDING_APPROVAL
                );
        }

        @Test
        @DisplayName("삭제된 인증은 조회되지 않아야 한다")
        void shouldNotFindDeletedVerifications() {
            // given
            LocalDateTime now = LocalDateTime.now();
            
            GroupChallengeVerification activeVerification = 
                GroupChallengeVerificationFixture.of(testParticipantRecord);
            verificationRepository.save(activeVerification);
            
            GroupChallengeVerification deletedVerification = 
                GroupChallengeVerificationFixture.of(testParticipantRecord);
            ReflectionTestUtils.setField(deletedVerification, "deletedAt", now);
            verificationRepository.save(deletedVerification);
            
            em.flush();
            em.clear();

            // when
            List<GroupChallengeVerification> results = 
                queryRepository.findByParticipantRecordId(testParticipantRecord.getId());

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getId()).isEqualTo(activeVerification.getId());
        }

        @Test
        @DisplayName("최신순으로 정렬되어야 한다")
        void shouldOrderByCreatedAtDesc() {
            // given
            LocalDateTime baseTime = LocalDateTime.now();
            
            GroupChallengeVerification oldestVerification = 
                createVerificationWithTime(baseTime.minusDays(2));
            GroupChallengeVerification middleVerification = 
                createVerificationWithTime(baseTime.minusDays(1));
            GroupChallengeVerification newestVerification = 
                createVerificationWithTime(baseTime);
            
            verificationRepository.save(oldestVerification);
            verificationRepository.save(middleVerification);
            verificationRepository.save(newestVerification);
            
            em.flush();
            em.clear();

            // when
            List<GroupChallengeVerification> results = 
                queryRepository.findByParticipantRecordId(testParticipantRecord.getId());

            // then
            assertThat(results).hasSize(3);
            // ID로 검증 (생성 순서를 확실히 알 수 있음)
            assertThat(results.get(0).getId()).isEqualTo(newestVerification.getId());
            assertThat(results.get(1).getId()).isEqualTo(middleVerification.getId());
            assertThat(results.get(2).getId()).isEqualTo(oldestVerification.getId());
            
            // 시간 순서 검증 (정확한 시간이 아닌 순서만 검증)
            assertThat(results.get(0).getCreatedAt()).isAfter(results.get(1).getCreatedAt());
            assertThat(results.get(1).getCreatedAt()).isAfter(results.get(2).getCreatedAt());
        }
    }

    @Nested
    @DisplayName("findByChallengeIdAndId 메서드")
    class FindByChallengeIdAndId {

        @Test
        @DisplayName("지정된 챌린지와 인증 ID로 인증을 조회해야 한다")
        void shouldFindVerificationByChallengeIdAndId() {
            // given
            GroupChallengeVerification verification = 
                GroupChallengeVerificationFixture.of(testParticipantRecord);
            verificationRepository.save(verification);
            
            em.flush();
            em.clear();

            // when
            Optional<GroupChallengeVerification> result = 
                queryRepository.findByChallengeIdAndId(testChallenge.getId(), verification.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(verification.getId());
            assertThat(result.get().getParticipantRecord()).isNotNull();
            assertThat(result.get().getParticipantRecord().getGroupChallenge()).isNotNull();
            assertThat(result.get().getParticipantRecord().getMember()).isNotNull();
        }

        @Test
        @DisplayName("다른 챌린지의 인증 ID로 조회하면 빈 값을 반환해야 한다")
        void shouldReturnEmptyForDifferentChallengeId() {
            // given
            GroupChallengeVerification verification = 
                GroupChallengeVerificationFixture.of(testParticipantRecord);
            verificationRepository.save(verification);
            
            Member otherMember = MemberFixture.of("other@example.com", "다른사용자");
            ReflectionTestUtils.setField(otherMember, "treeLevel", testMember.getTreeLevel());
            otherMember = memberRepository.save(otherMember);
            
            GroupChallenge otherChallenge = GroupChallengeFixture.of(otherMember, testCategory);
            otherChallenge = challengeRepository.save(otherChallenge);
            
            em.flush();
            em.clear();

            // when
            Optional<GroupChallengeVerification> result = 
                queryRepository.findByChallengeIdAndId(otherChallenge.getId(), verification.getId());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("삭제된 인증은 조회되지 않아야 한다")
        void shouldNotFindDeletedVerification() {
            // given
            LocalDateTime now = LocalDateTime.now();
            
            GroupChallengeVerification verification = 
                GroupChallengeVerificationFixture.of(testParticipantRecord);
            ReflectionTestUtils.setField(verification, "deletedAt", now);
            verificationRepository.save(verification);
            
            em.flush();
            em.clear();

            // when
            Optional<GroupChallengeVerification> result = 
                queryRepository.findByChallengeIdAndId(testChallenge.getId(), verification.getId());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("fetch join이 올바르게 동작해야 한다")
        void shouldFetchJoinCorrectly() {
            // given
            GroupChallengeVerification verification = 
                GroupChallengeVerificationFixture.of(testParticipantRecord);
            verificationRepository.save(verification);
            
            em.flush();
            em.clear();

            // when
            Optional<GroupChallengeVerification> result = 
                queryRepository.findByChallengeIdAndId(testChallenge.getId(), verification.getId());

            // then
            assertThat(result).isPresent();
            // Fetch join 검증 - 연관 엔티티들이 프록시가 아닌 실제 객체여야 함
            GroupChallengeVerification found = result.get();
            assertThat(found.getParticipantRecord().getGroupChallenge().getTitle())
                .isEqualTo(testChallenge.getTitle());
            assertThat(found.getParticipantRecord().getMember().getId())
                .isEqualTo(testMember.getId());
        }
    }

    @Nested
    @DisplayName("findVerificationsGroupedByChallenge 메서드")
    class FindVerificationsGroupedByChallenge {

        @Test
        @DisplayName("여러 챌린지의 인증 기록을 챌린지별로 그룹화해야 한다")
        void shouldGroupVerificationsByChallenge() {
            // given
            // 첫 번째 챌린지
            GroupChallengeVerification verification1 = 
                GroupChallengeVerificationFixture.of(testParticipantRecord, ChallengeStatus.SUCCESS);
            GroupChallengeVerification verification2 = 
                GroupChallengeVerificationFixture.of(testParticipantRecord, ChallengeStatus.FAILURE);
            verificationRepository.save(verification1);
            verificationRepository.save(verification2);
            
            // 두 번째 챌린지
            GroupChallenge challenge2 = GroupChallengeFixture.of(testMember, testCategory);
            challenge2 = challengeRepository.save(challenge2);
            
            GroupChallengeParticipantRecord record2 = 
                GroupChallengeParticipantRecordFixture.of(challenge2, testMember);
            record2 = participantRecordRepository.save(record2);
            
            GroupChallengeVerification verification3 = 
                GroupChallengeVerificationFixture.of(record2, ChallengeStatus.SUCCESS);
            verificationRepository.save(verification3);
            
            em.flush();
            em.clear();

            // when
            Map<Long, List<GroupChallengeParticipationSummaryDto.AchievementRecordDto>> result = 
                queryRepository.findVerificationsGroupedByChallenge(
                    List.of(testChallenge.getId(), challenge2.getId()), 
                    testMember.getId()
                );

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(testChallenge.getId())).hasSize(2);
            assertThat(result.get(challenge2.getId())).hasSize(1);
        }

        @Test
        @DisplayName("인증 기록에 순차적인 day 번호가 부여되어야 한다")
        void shouldAssignSequentialDayNumbers() {
            // given
            LocalDateTime baseTime = LocalDateTime.now();
            
            for (int i = 0; i < 3; i++) {
                GroupChallengeVerification verification = 
                    GroupChallengeVerificationFixture.of(testParticipantRecord, ChallengeStatus.SUCCESS);
                ReflectionTestUtils.setField(verification, "createdAt", baseTime.plusDays(i));
                verificationRepository.save(verification);
            }
            
            em.flush();
            em.clear();

            // when
            Map<Long, List<GroupChallengeParticipationSummaryDto.AchievementRecordDto>> result = 
                queryRepository.findVerificationsGroupedByChallenge(
                    List.of(testChallenge.getId()), 
                    testMember.getId()
                );

            // then
            List<GroupChallengeParticipationSummaryDto.AchievementRecordDto> records = 
                result.get(testChallenge.getId());
            assertThat(records).hasSize(3);
            assertThat(records).extracting("day", "status")
                .containsExactly(
                    tuple(1, "SUCCESS"),
                    tuple(2, "SUCCESS"),
                    tuple(3, "SUCCESS")
                );
        }

        @Test
        @DisplayName("삭제된 인증은 제외되어야 한다")
        void shouldExcludeDeletedVerifications() {
            // given
            LocalDateTime now = LocalDateTime.now();
            
            GroupChallengeVerification activeVerification = 
                GroupChallengeVerificationFixture.of(testParticipantRecord);
            verificationRepository.save(activeVerification);
            
            GroupChallengeVerification deletedVerification = 
                GroupChallengeVerificationFixture.of(testParticipantRecord);
            ReflectionTestUtils.setField(deletedVerification, "deletedAt", now);
            verificationRepository.save(deletedVerification);
            
            em.flush();
            em.clear();

            // when
            Map<Long, List<GroupChallengeParticipationSummaryDto.AchievementRecordDto>> result = 
                queryRepository.findVerificationsGroupedByChallenge(
                    List.of(testChallenge.getId()), 
                    testMember.getId()
                );

            // then
            assertThat(result.get(testChallenge.getId())).hasSize(1);
        }

        @Test
        @DisplayName("다른 회원의 인증은 포함되지 않아야 한다")
        void shouldNotIncludeOtherMemberVerifications() {
            // given
            GroupChallengeVerification myVerification = 
                GroupChallengeVerificationFixture.of(testParticipantRecord);
            verificationRepository.save(myVerification);
            
            // 다른 회원의 인증
            Member otherMember = MemberFixture.of("other@example.com", "다른사용자");
            ReflectionTestUtils.setField(otherMember, "treeLevel", testMember.getTreeLevel());
            otherMember = memberRepository.save(otherMember);
            
            GroupChallengeParticipantRecord otherRecord = 
                GroupChallengeParticipantRecordFixture.of(testChallenge, otherMember);
            otherRecord = participantRecordRepository.save(otherRecord);
            
            GroupChallengeVerification otherVerification = 
                GroupChallengeVerificationFixture.of(otherRecord);
            verificationRepository.save(otherVerification);
            
            em.flush();
            em.clear();

            // when
            Map<Long, List<GroupChallengeParticipationSummaryDto.AchievementRecordDto>> result = 
                queryRepository.findVerificationsGroupedByChallenge(
                    List.of(testChallenge.getId()), 
                    testMember.getId()
                );

            // then
            assertThat(result.get(testChallenge.getId())).hasSize(1);
        }

        @Test
        @DisplayName("생성 시간 순서대로 정렬되어야 한다")
        void shouldOrderByCreatedAtAsc() {
            // given
            LocalDateTime baseTime = LocalDateTime.now();
            
            GroupChallengeVerification verification3 = 
                createVerificationWithTime(baseTime.plusDays(2));
            GroupChallengeVerification verification1 = 
                createVerificationWithTime(baseTime);
            GroupChallengeVerification verification2 = 
                createVerificationWithTime(baseTime.plusDays(1));
            
            verificationRepository.save(verification3);
            verificationRepository.save(verification1);
            verificationRepository.save(verification2);
            
            em.flush();
            em.clear();

            // when
            Map<Long, List<GroupChallengeParticipationSummaryDto.AchievementRecordDto>> result = 
                queryRepository.findVerificationsGroupedByChallenge(
                    List.of(testChallenge.getId()), 
                    testMember.getId()
                );

            // then
            List<GroupChallengeParticipationSummaryDto.AchievementRecordDto> records = 
                result.get(testChallenge.getId());
            assertThat(records).extracting("day")
                .containsExactly(1, 2, 3);
        }
    }

    // Helper methods
    private List<GroupChallengeVerification> createMultipleVerifications(int count, LocalDateTime baseTime) {
        return java.util.stream.IntStream.range(0, count)
            .mapToObj(i -> {
                GroupChallengeVerification verification = 
                    GroupChallengeVerificationFixture.of(testParticipantRecord);
                ReflectionTestUtils.setField(verification, "createdAt", baseTime.minusHours(i));
                verificationRepository.save(verification);
                return verification;
            })
            .toList();
    }

    private GroupChallengeVerification createVerificationWithTime(LocalDateTime time) {
        GroupChallengeVerification verification = 
            GroupChallengeVerificationFixture.of(testParticipantRecord);
        ReflectionTestUtils.setField(verification, "createdAt", time);
        return verification;
    }
}
