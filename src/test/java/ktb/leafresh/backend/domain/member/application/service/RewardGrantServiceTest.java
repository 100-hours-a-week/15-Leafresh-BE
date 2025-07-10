package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.member.application.service.updater.LeafPointCacheUpdater;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
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

    @InjectMocks
    private RewardGrantService rewardGrantService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = MemberFixture.of();
        member.updateCurrentLeafPoints(0);
    }

    @Nested
    @DisplayName("grantLeafPoints")
    class GrantLeafPoints {

        @Test
        @DisplayName("지정한 포인트만큼 회원에게 나뭇잎을 지급하고 캐시에 반영한다")
        void grantLeafPoints_success() {
            // when
            rewardGrantService.grantLeafPoints(member, 500);

            // then
            assertThat(member.getCurrentLeafPoints()).isEqualTo(500);
            assertThat(member.getTotalLeafPoints()).isEqualTo(500);
            verify(rewardService).rewardLeafPoints(member, 500);
        }
    }

    @Nested
    @DisplayName("grantSignupReward")
    class GrantSignupReward {

        @Test
        @DisplayName("회원가입 보상으로 1500포인트를 지급한다")
        void grantSignupReward_success() {
            // when
            rewardGrantService.grantSignupReward(member);

            // then
            assertThat(member.getCurrentLeafPoints()).isEqualTo(1500);
            assertThat(member.getTotalLeafPoints()).isEqualTo(1500);
            verify(rewardService).rewardLeafPoints(member, 1500);
        }
    }

    @Nested
    @DisplayName("grantDailyLoginReward")
    class GrantDailyLoginReward {

        @Test
        @DisplayName("오늘 로그인 보상을 아직 받지 않았다면 30포인트를 지급한다")
        void grantDailyLoginReward_success() {
            // when
            rewardGrantService.grantDailyLoginReward(member);

            // then
            assertThat(member.getCurrentLeafPoints()).isEqualTo(30);
            assertThat(member.getTotalLeafPoints()).isEqualTo(30);
            verify(rewardService).rewardLeafPoints(member, 30);
        }

        @Test
        @DisplayName("이미 오늘 보상을 받은 경우 지급하지 않는다")
        void grantDailyLoginReward_skipIfAlreadyReceived() {
            // given
            member.updateLastLoginRewardedAt(); // 오늘로 설정됨
            int before = member.getCurrentLeafPoints();

            // when
            rewardGrantService.grantDailyLoginReward(member);

            // then
            assertThat(member.getCurrentLeafPoints()).isEqualTo(before); // unchanged
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
        @DisplayName("5일 이하 챌린지 보너스로 50포인트 지급")
        void grantParticipationBonus_shortChallenge() {
            // given
            given(record.getGroupChallenge()).willReturn(challenge);
            given(challenge.getDurationInDays()).willReturn(3);

            // when
            rewardGrantService.grantParticipationBonus(member, record);

            // then
            assertThat(member.getCurrentLeafPoints()).isEqualTo(50);
            verify(rewardService).rewardLeafPoints(member, 50);
        }

        @Test
        @DisplayName("10일 이하 챌린지 보너스로 100포인트 지급")
        void grantParticipationBonus_mediumChallenge() {
            given(record.getGroupChallenge()).willReturn(challenge);
            given(challenge.getDurationInDays()).willReturn(10);

            rewardGrantService.grantParticipationBonus(member, record);

            assertThat(member.getCurrentLeafPoints()).isEqualTo(100);
            verify(rewardService).rewardLeafPoints(member, 100);
        }

        @Test
        @DisplayName("15일 이하 챌린지 보너스로 150포인트 지급")
        void grantParticipationBonus_longChallenge() {
            given(record.getGroupChallenge()).willReturn(challenge);
            given(challenge.getDurationInDays()).willReturn(15);

            rewardGrantService.grantParticipationBonus(member, record);

            assertThat(member.getCurrentLeafPoints()).isEqualTo(150);
            verify(rewardService).rewardLeafPoints(member, 150);
        }

        @Test
        @DisplayName("16일 이상 챌린지 보너스로 200포인트 지급")
        void grantParticipationBonus_extraLongChallenge() {
            given(record.getGroupChallenge()).willReturn(challenge);
            given(challenge.getDurationInDays()).willReturn(30);

            rewardGrantService.grantParticipationBonus(member, record);

            assertThat(member.getCurrentLeafPoints()).isEqualTo(200);
            verify(rewardService).rewardLeafPoints(member, 200);
        }
    }
}
