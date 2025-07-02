//package ktb.leafresh.backend.domain.member.application.service;
//
//import ktb.leafresh.backend.domain.member.domain.entity.Badge;
//import ktb.leafresh.backend.domain.member.domain.entity.Member;
//import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
//import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
//import ktb.leafresh.backend.domain.member.presentation.dto.response.BadgeListResponseDto;
//import ktb.leafresh.backend.domain.member.presentation.dto.response.BadgeResponseDto;
//import ktb.leafresh.backend.global.exception.CustomException;
//import ktb.leafresh.backend.global.exception.MemberErrorCode;
//import ktb.leafresh.backend.support.fixture.BadgeFixture;
//import ktb.leafresh.backend.support.fixture.MemberBadgeFixture;
//import ktb.leafresh.backend.support.fixture.MemberFixture;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//
//import java.util.*;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//@ExtendWith(MockitoExtension.class)
//class BadgeReadServiceTest {
//
//    @Mock
//    private BadgeRepository badgeRepository;
//
//    @Mock
//    private MemberRepository memberRepository;
//
//    @InjectMocks
//    private BadgeReadService badgeReadService;
//
//    @Test
//    @DisplayName("getAllBadges_일부획득시_획득여부구분하여반환")
//    void getAllBadges_givenPartialAcquisition_thenReturnWithLockStatus() {
//        // given
//        Member member = MemberFixture.of();
//        Badge acquiredBadge = BadgeFixture.of("습지 전도사");
//        Badge lockedBadge = BadgeFixture.of("지속가능 전도사");
//
//        member.getMemberBadges().add(MemberBadgeFixture.of(member, acquiredBadge));
//
//        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
//        when(badgeRepository.findAll()).thenReturn(List.of(acquiredBadge, lockedBadge));
//
//        // when
//        BadgeListResponseDto response = badgeReadService.getAllBadges(member.getId());
//
//        // then
//        List<BadgeResponseDto> badges = response.getBadges().get(acquiredBadge.getType());
//
//        assertThat(badges).hasSize(2);
//        assertThat(badges)
//                .anySatisfy(b -> assertThat(b.getName()).isEqualTo(acquiredBadge.getName())
//                        .isNotNull())
//                .anySatisfy(b -> assertThat(b.getName()).isEqualTo(lockedBadge.getName())
//                        .isNotNull());
//
//        assertThat(badges)
//                .anySatisfy(b -> assertThat(b.isLocked()).isFalse())
//                .anySatisfy(b -> assertThat(b.isLocked()).isTrue());
//    }
//
//    @Test
//    @DisplayName("getAllBadges_회원없을때_예외발생")
//    void getAllBadges_givenInvalidMemberId_thenThrowNotFound() {
//        // given
//        Long invalidMemberId = 999L;
//        when(memberRepository.findById(invalidMemberId)).thenReturn(Optional.empty());
//
//        // expect
//        assertThatThrownBy(() -> badgeReadService.getAllBadges(invalidMemberId))
//                .isInstanceOf(CustomException.class)
//                .hasMessageContaining(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
//    }
//
//    @Test
//    @DisplayName("getAllBadges_뱃지없을때_예외발생")
//    void getAllBadges_givenEmptyBadgeList_thenThrowBadgeQueryFailed() {
//        // given
//        Member member = MemberFixture.of();
//        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));
//        when(badgeRepository.findAll()).thenReturn(Collections.emptyList());
//
//        // expect
//        assertThatThrownBy(() -> badgeReadService.getAllBadges(member.getId()))
//                .isInstanceOf(CustomException.class)
//                .hasMessageContaining(MemberErrorCode.BADGE_QUERY_FAILED.getMessage());
//    }
//}
