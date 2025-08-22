package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.MemberBadge;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.RecentBadgeListResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.support.fixture.BadgeFixture;
import ktb.leafresh.backend.support.fixture.MemberBadgeFixture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static ktb.leafresh.backend.support.fixture.MemberFixture.of;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecentBadgeReadServiceTest {

  @Mock private MemberRepository memberRepository;

  @Mock private MemberBadgeRepository memberBadgeRepository;

  @InjectMocks private RecentBadgeReadService service;

  @Test
  @DisplayName("최근 뱃지 조회 - 유효한 회원 ID일 경우 - 뱃지 리스트를 반환한다")
  void 최근뱃지조회_유효한회원ID일경우_뱃지리스트반환() {
    // given
    Member member = of("test@leafresh.com", "테스터");
    Badge badge = BadgeFixture.of("참가왕");
    MemberBadge memberBadge = MemberBadgeFixture.of(member, badge);

    when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
    when(memberBadgeRepository.findRecentBadgesByMemberId(1L, 3)).thenReturn(List.of(memberBadge));

    // when
    RecentBadgeListResponseDto response = service.getRecentBadges(1L, 3);

    // then
    assertThat(response.badges()).hasSize(1);
    assertThat(response.badges().get(0).name()).isEqualTo(badge.getName());
    assertThat(response.badges().get(0).condition()).isEqualTo(badge.getCondition());
    assertThat(response.badges().get(0).imageUrl()).isEqualTo(badge.getImageUrl());
  }

  @Test
  @DisplayName("최근 뱃지 조회 - 존재하지 않는 회원 ID일 경우 - MEMBER_NOT_FOUND 예외를 반환한다")
  void 최근뱃지조회_존재하지않는회원ID일경우_MEMBER_NOT_FOUND예외발생() {
    // given
    when(memberRepository.findById(1L)).thenReturn(Optional.empty());

    // when
    CustomException exception =
        catchThrowableOfType(() -> service.getRecentBadges(1L, 3), CustomException.class);

    // then
    assertThat(exception).isNotNull();
    assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
  }

  @Test
  @DisplayName("최근 뱃지 조회 - 내부 오류 발생 시 - BADGE_QUERY_FAILED 예외를 반환한다")
  void 최근뱃지조회_내부오류발생시_BADGE_QUERY_FAILED예외발생() {
    // given
    Member member = of("test@leafresh.com", "테스터");
    when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
    when(memberBadgeRepository.findRecentBadgesByMemberId(anyLong(), anyInt()))
        .thenThrow(new RuntimeException("DB Error"));

    // when
    CustomException exception =
        catchThrowableOfType(() -> service.getRecentBadges(1L, 3), CustomException.class);

    // then
    assertThat(exception).isNotNull();
    assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.BADGE_QUERY_FAILED);
  }
}
