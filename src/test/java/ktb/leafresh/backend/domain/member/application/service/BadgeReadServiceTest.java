package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.MemberBadge;
import ktb.leafresh.backend.domain.member.domain.entity.enums.BadgeType;
import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.BadgeListResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.support.fixture.BadgeFixture;
import ktb.leafresh.backend.support.fixture.MemberBadgeFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static ktb.leafresh.backend.global.exception.MemberErrorCode.BADGE_QUERY_FAILED;
import static ktb.leafresh.backend.global.exception.MemberErrorCode.MEMBER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

class BadgeReadServiceTest {

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private BadgeReadService badgeReadService;

    private Member member;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        member = MemberFixture.of();
    }

    @Test
    @DisplayName("회원이 보유한 뱃지를 포함하여 전체 뱃지 조회")
    void getAllBadges_success() {
        // given
        Badge owned = BadgeFixture.of("지속가능 전도사", BadgeType.SPECIAL);
        Badge locked = BadgeFixture.of("에코 슈퍼루키", BadgeType.SPECIAL);

        ReflectionTestUtils.setField(owned, "id", 1L);
        ReflectionTestUtils.setField(locked, "id", 2L);

        MemberBadge memberBadge = MemberBadgeFixture.of(member, owned);
        member.getMemberBadges().add(memberBadge);

        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(badgeRepository.findAll()).willReturn(List.of(owned, locked));

        // when
        BadgeListResponseDto response = badgeReadService.getAllBadges(member.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getBadges()).containsKey("special");
        assertThat(response.getBadges().get("special")).hasSize(2);

        assertThat(response.getBadges().get("special").get(0).isLocked()).isFalse();
        assertThat(response.getBadges().get("special").get(1).isLocked()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 회원인 경우 예외 발생")
    void getAllBadges_memberNotFound() {
        // given
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> badgeReadService.getAllBadges(999L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("뱃지 데이터가 없는 경우 예외 발생")
    void getAllBadges_noBadges() {
        // given
        given(memberRepository.findById(member.getId())).willReturn(Optional.of(member));
        given(badgeRepository.findAll()).willReturn(List.of());

        // expect
        assertThatThrownBy(() -> badgeReadService.getAllBadges(member.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(BADGE_QUERY_FAILED.getMessage());
    }
}
