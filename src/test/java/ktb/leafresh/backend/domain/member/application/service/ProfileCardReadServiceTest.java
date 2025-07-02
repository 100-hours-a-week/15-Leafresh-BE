package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.domain.entity.enums.TreeLevelName;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.TreeLevelRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.ProfileCardResponseDto;
import ktb.leafresh.backend.domain.member.presentation.dto.response.ProfileCardResponseDto.RecentBadgeDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import ktb.leafresh.backend.support.fixture.TreeLevelFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileCardReadServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberBadgeRepository memberBadgeRepository;

    @Mock
    private TreeLevelRepository treeLevelRepository;

    @InjectMocks
    private ProfileCardReadService profileCardReadService;

    private Member member;
    private TreeLevel currentLevel;
    private TreeLevel nextLevel;

    @BeforeEach
    void setUp() {
        currentLevel = TreeLevelFixture.of(TreeLevelName.SPROUT);
        nextLevel = TreeLevelFixture.of(TreeLevelName.YOUNG);
        ReflectionTestUtils.setField(currentLevel, "minLeafPoint", 0);
        ReflectionTestUtils.setField(nextLevel, "minLeafPoint", 100);

        member = MemberFixture.of();
        ReflectionTestUtils.setField(member, "treeLevel", currentLevel);
        ReflectionTestUtils.setField(member, "totalLeafPoints", 50);
        ReflectionTestUtils.setField(member, "id", 1L);
    }

    @Nested
    @DisplayName("getProfileCard")
    class GetProfileCard {

        @Test
        @DisplayName("정상적으로 프로필 카드를 조회한다.")
        void getProfileCard_success() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(treeLevelRepository.findFirstByMinLeafPointGreaterThanOrderByMinLeafPointAsc(0))
                    .willReturn(Optional.of(nextLevel));
            given(memberBadgeRepository.findRecentBadgesByMemberId(1L, 3))
                    .willReturn(Collections.emptyList());

            // when
            ProfileCardResponseDto response = profileCardReadService.getProfileCard(1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.nickname()).isEqualTo(member.getNickname());
            assertThat(response.profileImageUrl()).isEqualTo(member.getImageUrl());
            assertThat(response.treeLevelName()).isEqualTo(currentLevel.getName().name());
            assertThat(response.nextTreeLevelName()).isEqualTo(nextLevel.getName().name());
            assertThat(response.totalLeafPoints()).isEqualTo(50);
            assertThat(response.leafPointsToNextLevel()).isEqualTo(50);
            assertThat(response.badges()).isEmpty();
        }

        @Test
        @DisplayName("다음 TreeLevel이 없는 경우 leafPointsToNextLevel은 0이 된다.")
        void getProfileCard_noNextTreeLevel() {
            // given
            given(memberRepository.findById(1L)).willReturn(Optional.of(member));
            given(treeLevelRepository.findFirstByMinLeafPointGreaterThanOrderByMinLeafPointAsc(anyInt()))
                    .willReturn(Optional.empty());
            given(memberBadgeRepository.findRecentBadgesByMemberId(1L, 3))
                    .willReturn(Collections.emptyList());

            // when
            ProfileCardResponseDto response = profileCardReadService.getProfileCard(1L);

            // then
            assertThat(response.nextTreeLevelName()).isNull();
            assertThat(response.leafPointsToNextLevel()).isEqualTo(0);
        }

        @Test
        @DisplayName("회원이 존재하지 않으면 PROFILE_CARD_NOT_FOUND 예외를 던진다.")
        void getProfileCard_memberNotFound() {
            // given
            given(memberRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> profileCardReadService.getProfileCard(999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(MemberErrorCode.PROFILE_CARD_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("예상치 못한 예외가 발생하면 PROFILE_CARD_QUERY_FAILED 예외를 던진다.")
        void getProfileCard_unexpectedException() {
            // given
            given(memberRepository.findById(anyLong()))
                    .willThrow(new RuntimeException("DB 연결 실패"));

            // when & then
            assertThatThrownBy(() -> profileCardReadService.getProfileCard(1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(MemberErrorCode.PROFILE_CARD_QUERY_FAILED.getMessage());
        }
    }
}
