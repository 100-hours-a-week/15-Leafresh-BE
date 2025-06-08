package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.member.application.service.updater.LeafPointCacheUpdater;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeParticipantRecordFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RewardGrantServiceTest {

    private LeafPointCacheUpdater rewardService;
    private RewardGrantService rewardGrantService;

    @BeforeEach
    void setUp() {
        rewardService = mock(LeafPointCacheUpdater.class);
        rewardGrantService = new RewardGrantService(rewardService);
    }

    @Test
    @DisplayName("회원가입 보상으로 나뭇잎 1500 지급")
    void grantSignupReward() {
        Member member = MemberFixture.of();
        rewardGrantService.grantSignupReward(member);

        assertThat(member.getCurrentLeafPoints()).isEqualTo(1500);
        assertThat(member.getTotalLeafPoints()).isEqualTo(1500);
        verify(rewardService).rewardLeafPoints(member, 1500);
    }

    @Test
    @DisplayName("일일 로그인 보상은 하루에 한 번만 지급")
    void grantDailyLoginReward_oncePerDay() {
        Member member = MemberFixture.of();

        // 첫 지급
        rewardGrantService.grantDailyLoginReward(member);
        assertThat(member.getCurrentLeafPoints()).isEqualTo(10);

        // 같은 날 재지급 시도
        rewardGrantService.grantDailyLoginReward(member);
        assertThat(member.getCurrentLeafPoints()).isEqualTo(10); // 그대로

        verify(rewardService, times(1)).rewardLeafPoints(member, 10);
    }

    @Test
    @DisplayName("인증 기간에 따라 보너스 지급")
    void grantParticipationBonus() {
        Member member = MemberFixture.of();
        GroupChallengeCategory category = GroupChallengeCategoryFixture.of("환경");
        GroupChallenge challenge = GroupChallengeFixture.of(member, category); // 실제 15일 챌린지
        GroupChallengeParticipantRecord record = GroupChallengeParticipantRecordFixture.of(challenge, member);

        rewardGrantService.grantParticipationBonus(member, record);

        assertThat(member.getCurrentLeafPoints()).isEqualTo(150); // 실제 기간: 15일 → 150 보너스
        verify(rewardService).rewardLeafPoints(member, 150);
    }

    @Test
    @DisplayName("grantLeafPoints는 현재/누적 포인트에 모두 반영")
    void grantLeafPoints_direct() {
        Member member = MemberFixture.of();
        rewardGrantService.grantLeafPoints(member, 777);

        assertThat(member.getCurrentLeafPoints()).isEqualTo(777);
        assertThat(member.getTotalLeafPoints()).isEqualTo(777);
        verify(rewardService).rewardLeafPoints(member, 777);
    }
}
