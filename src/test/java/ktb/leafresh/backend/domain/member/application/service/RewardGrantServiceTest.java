package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.member.application.service.updater.LeafPointCacheUpdater;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.domain.entity.enums.TreeLevelName;
import ktb.leafresh.backend.domain.member.infrastructure.repository.TreeLevelRepository;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RewardGrantServiceTest {

    @Mock
    private LeafPointCacheUpdater rewardService;

    @Mock
    private TreeLevelRepository treeLevelRepository;

    @InjectMocks
    private RewardGrantService rewardGrantService;

    private Member member;
    private TreeLevel sprout;
    private TreeLevel young;

    @BeforeEach
    void setUp() {
        sprout = TreeLevel.builder().id(1L).name(TreeLevelName.SPROUT).minLeafPoint(0).build();
        young = TreeLevel.builder().id(2L).name(TreeLevelName.YOUNG).minLeafPoint(2500).build();

        member = MemberFixture.of();
        member.updateCurrentLeafPoints(0);
        member.updateTreeLevel(sprout);
    }

    @Nested
    @DisplayName("grantLeafPoints")
    class GrantLeafPoints {

        @Test
        @DisplayName("지정한 포인트만큼 회원에게 나뭇잎을 지급하고 캐시에 반영하며 트리 레벨도 갱신한다")
        void grantLeafPoints_withTreeLevelUpgrade() {
            // given
            given(treeLevelRepository.findFirstByMinLeafPointLessThanEqualOrderByMinLeafPointDesc(3000))
                    .willReturn(java.util.Optional.of(young));

            // when
            rewardGrantService.grantLeafPoints(member, 3000);

            // then
            assertThat(member.getCurrentLeafPoints()).isEqualTo(3000);
            assertThat(member.getTotalLeafPoints()).isEqualTo(3000);
            assertThat(member.getTreeLevel()).isEqualTo(young);
            verify(rewardService).rewardLeafPoints(member, 3000);
        }

        @Test
        @DisplayName("트리 레벨이 변경되지 않는 경우 기존 레벨 유지")
        void grantLeafPoints_withoutTreeLevelUpgrade() {
            // given
            given(treeLevelRepository.findFirstByMinLeafPointLessThanEqualOrderByMinLeafPointDesc(500))
                    .willReturn(java.util.Optional.of(sprout));

            // when
            rewardGrantService.grantLeafPoints(member, 500);

            // then
            assertThat(member.getTreeLevel()).isEqualTo(sprout);
            assertThat(member.getCurrentLeafPoints()).isEqualTo(500);
            assertThat(member.getTotalLeafPoints()).isEqualTo(500);
            verify(rewardService).rewardLeafPoints(member, 500);
        }
    }

    @Nested
    @DisplayName("grantSignupReward")
    class GrantSignupReward {

        @Test
        @DisplayName("회원가입 보상으로 1500포인트를 지급하고 트리 레벨을 갱신한다")
        void grantSignupReward_success() {
            given(treeLevelRepository.findFirstByMinLeafPointLessThanEqualOrderByMinLeafPointDesc(1500))
                    .willReturn(java.util.Optional.of(sprout));

            rewardGrantService.grantSignupReward(member);

            assertThat(member.getCurrentLeafPoints()).isEqualTo(1500);
            assertThat(member.getTreeLevel()).isEqualTo(sprout);
            verify(rewardService).rewardLeafPoints(member, 1500);
        }
    }

    @Nested
    @DisplayName("grantDailyLoginReward")
    class GrantDailyLoginReward {

        @Test
        @DisplayName("오늘 로그인 보상을 아직 받지 않았다면 30포인트를 지급하고 트리 레벨을 갱신한다")
        void grantDailyLoginReward_success() {
            given(treeLevelRepository.findFirstByMinLeafPointLessThanEqualOrderByMinLeafPointDesc(30))
                    .willReturn(java.util.Optional.of(sprout));

            rewardGrantService.grantDailyLoginReward(member);

            assertThat(member.getCurrentLeafPoints()).isEqualTo(30);
            assertThat(member.hasReceivedLoginRewardToday()).isTrue();
            verify(rewardService).rewardLeafPoints(member, 30);
        }

        @Test
        @DisplayName("이미 오늘 보상을 받은 경우 지급하지 않는다")
        void grantDailyLoginReward_skipIfAlreadyReceived() {
            // given
            member.updateLastLoginRewardedAt();
            int before = member.getCurrentLeafPoints();

            // when
            rewardGrantService.grantDailyLoginReward(member);

            // then
            assertThat(member.getCurrentLeafPoints()).isEqualTo(before);
            verify(rewardService, never()).rewardLeafPoints(any(), anyInt());
        }
    }

    @Nested
    @DisplayName("grantParticipationBonus")
    class GrantParticipationBonus {

        @Mock
        private GroupChallengeParticipantRecord record;

        @Mock
        private GroupChallenge challenge;

        @Test
        @DisplayName("5일 이하 챌린지 보너스로 50포인트 지급 후 트리 레벨 갱신")
        void grantParticipationBonus_shortChallenge() {
            given(record.getGroupChallenge()).willReturn(challenge);
            given(challenge.getDurationInDays()).willReturn(3);
            given(treeLevelRepository.findFirstByMinLeafPointLessThanEqualOrderByMinLeafPointDesc(50))
                    .willReturn(java.util.Optional.of(sprout));

            rewardGrantService.grantParticipationBonus(member, record);

            assertThat(member.getCurrentLeafPoints()).isEqualTo(50);
            assertThat(member.getTreeLevel()).isEqualTo(sprout);
            verify(rewardService).rewardLeafPoints(member, 50);
        }

        @Test
        @DisplayName("10일 이하 챌린지 보너스로 100포인트 지급")
        void grantParticipationBonus_mediumChallenge() {
            given(record.getGroupChallenge()).willReturn(challenge);
            given(challenge.getDurationInDays()).willReturn(10);
            given(treeLevelRepository.findFirstByMinLeafPointLessThanEqualOrderByMinLeafPointDesc(100))
                    .willReturn(java.util.Optional.of(sprout));

            rewardGrantService.grantParticipationBonus(member, record);

            assertThat(member.getCurrentLeafPoints()).isEqualTo(100);
            verify(rewardService).rewardLeafPoints(member, 100);
        }

        @Test
        @DisplayName("15일 이하 챌린지 보너스로 150포인트 지급")
        void grantParticipationBonus_longChallenge() {
            given(record.getGroupChallenge()).willReturn(challenge);
            given(challenge.getDurationInDays()).willReturn(15);
            given(treeLevelRepository.findFirstByMinLeafPointLessThanEqualOrderByMinLeafPointDesc(150))
                    .willReturn(java.util.Optional.of(sprout));

            rewardGrantService.grantParticipationBonus(member, record);

            assertThat(member.getCurrentLeafPoints()).isEqualTo(150);
            verify(rewardService).rewardLeafPoints(member, 150);
        }

        @Test
        @DisplayName("16일 이상 챌린지 보너스로 200포인트 지급 후 트리 레벨 갱신")
        void grantParticipationBonus_extraLongChallenge() {
            given(record.getGroupChallenge()).willReturn(challenge);
            given(challenge.getDurationInDays()).willReturn(30);
            given(treeLevelRepository.findFirstByMinLeafPointLessThanEqualOrderByMinLeafPointDesc(200))
                    .willReturn(java.util.Optional.of(sprout));

            rewardGrantService.grantParticipationBonus(member, record);

            assertThat(member.getCurrentLeafPoints()).isEqualTo(200);
            verify(rewardService).rewardLeafPoints(member, 200);
        }
    }
}
