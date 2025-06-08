package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.MemberBadge;
import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.domain.entity.enums.TreeLevelName;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.TreeLevelRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.ProfileCardResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.support.fixture.BadgeFixture;
import ktb.leafresh.backend.support.fixture.MemberBadgeFixture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static ktb.leafresh.backend.support.fixture.TreeLevelFixture.defaultLevel;
import static ktb.leafresh.backend.support.fixture.MemberFixture.of;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileCardReadServiceTest {

    private MemberRepository memberRepository;
    private MemberBadgeRepository memberBadgeRepository;
    private TreeLevelRepository treeLevelRepository;
    private ProfileCardReadService service;

    private Member member;
    private TreeLevel currentLevel;
    private TreeLevel nextLevel;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        memberBadgeRepository = mock(MemberBadgeRepository.class);
        treeLevelRepository = mock(TreeLevelRepository.class);

        service = new ProfileCardReadService(
                memberRepository,
                memberBadgeRepository,
                treeLevelRepository
        );

        currentLevel = defaultLevel();
        member = of(1L, "test@leafresh.com", "테스터", currentLevel);

        // ❗ NullPointerException 방지를 위해 컬렉션 비워두기
        member.getGroupChallengeParticipantRecords().clear();
        member.getPersonalChallengeVerifications().clear();

        nextLevel = TreeLevel.builder()
                .id(2L)
                .name(TreeLevelName.TREE) // enum 상수이므로 문자열 비교 가능
                .minLeafPoint(10)
                .imageUrl("https://dummy.image/tree/next.png")
                .description("다음 단계 레벨")
                .build();
    }

    @Test
    @DisplayName("정상적으로 프로필 카드 정보를 조회한다")
    void getProfileCard_success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(treeLevelRepository.findFirstByMinLeafPointGreaterThanOrderByMinLeafPointAsc(anyInt()))
                .thenReturn(Optional.of(nextLevel));

        Badge badge = BadgeFixture.of(1L, "나무");
        MemberBadge memberBadge = MemberBadgeFixture.of(member, badge);
        when(memberBadgeRepository.findRecentBadgesByMemberId(1L, 3)).thenReturn(List.of(memberBadge));

        ProfileCardResponseDto response = service.getProfileCard(1L);

        assertThat(response.nickname()).isEqualTo("테스터");
        assertThat(response.treeLevelName()).isEqualTo("SPROUT");
        assertThat(response.nextTreeLevelName()).isEqualTo("TREE");
        assertThat(response.badges()).hasSize(1);
        assertThat(response.badges().get(0).name()).isEqualTo("나무");
    }

    @Test
    @DisplayName("존재하지 않는 회원일 경우 예외를 발생시킨다")
    void getProfileCard_fail_memberNotFound() {
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        CustomException exception = catchThrowableOfType(() -> service.getProfileCard(1L), CustomException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.PROFILE_CARD_NOT_FOUND);
    }

    @Test
    @DisplayName("다음 트리 레벨이 없을 경우 null로 처리된다")
    void getProfileCard_withMaxLevel() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(treeLevelRepository.findFirstByMinLeafPointGreaterThanOrderByMinLeafPointAsc(anyInt()))
                .thenReturn(Optional.empty());
        when(memberBadgeRepository.findRecentBadgesByMemberId(1L, 3)).thenReturn(List.of());

        ProfileCardResponseDto response = service.getProfileCard(1L);

        assertThat(response.nextTreeLevelName()).isNull();
        assertThat(response.badges()).isEmpty();
    }

    @Test
    @DisplayName("예상치 못한 오류가 발생하면 PROFILE_CARD_QUERY_FAILED 예외를 발생시킨다")
    void getProfileCard_unexpectedError() {
        when(memberRepository.findById(anyLong())).thenThrow(new RuntimeException("DB 에러"));

        CustomException exception = catchThrowableOfType(() -> service.getProfileCard(1L), CustomException.class);

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.PROFILE_CARD_QUERY_FAILED);
    }
}
