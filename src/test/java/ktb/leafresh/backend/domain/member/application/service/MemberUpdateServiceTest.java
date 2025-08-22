package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.MemberUpdateResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MemberUpdateServiceTest {

  @Mock private MemberRepository memberRepository;

  @InjectMocks private MemberUpdateService memberUpdateService;

  private Member member;
  private Long memberId;

  @BeforeEach
  void setUp() {
    member = MemberFixture.of("user@leafresh.com", "기존닉네임");
    memberId = 1L;
    ReflectionTestUtils.setField(member, "id", memberId);
  }

  @Nested
  @DisplayName("updateMemberInfo")
  class UpdateMemberInfo {

    @Test
    @DisplayName("닉네임과 이미지 URL이 모두 변경되면 성공적으로 응답을 반환한다.")
    void updateBothNicknameAndImage_success() {
      // given
      String newNickname = "새닉네임";
      String newImageUrl = "https://new.image/profile.png";
      given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
      given(memberRepository.existsByNicknameAndIdNot(newNickname, member.getId()))
          .willReturn(false);

      // when
      MemberUpdateResponseDto response =
          memberUpdateService.updateMemberInfo(memberId, newNickname, newImageUrl);

      // then
      assertThat(response).isNotNull();
      assertThat(response.getNickname()).isEqualTo(newNickname);
      assertThat(response.getImageUrl()).isEqualTo(newImageUrl);
    }

    @Test
    @DisplayName("닉네임만 변경되는 경우 성공적으로 응답을 반환한다.")
    void updateNicknameOnly_success() {
      // given
      String newNickname = "변경된닉네임";
      given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
      given(memberRepository.existsByNicknameAndIdNot(newNickname, member.getId()))
          .willReturn(false);

      // when
      MemberUpdateResponseDto response =
          memberUpdateService.updateMemberInfo(memberId, newNickname, member.getImageUrl());

      // then
      assertThat(response.getNickname()).isEqualTo(newNickname);
      assertThat(response.getImageUrl()).isEqualTo(member.getImageUrl());
    }

    @Test
    @DisplayName("이미지 URL만 변경되는 경우 성공적으로 응답을 반환한다.")
    void updateImageOnly_success() {
      // given
      String newImageUrl = "https://changed.image/new.png";
      given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

      // when
      MemberUpdateResponseDto response =
          memberUpdateService.updateMemberInfo(memberId, member.getNickname(), newImageUrl);

      // then
      assertThat(response.getNickname()).isEqualTo(member.getNickname());
      assertThat(response.getImageUrl()).isEqualTo(newImageUrl);
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 요청하면 MEMBER_NOT_FOUND 예외를 던진다.")
    void updateMemberInfo_memberNotFound_throwsException() {
      // given
      Long nonExistentMemberId = 999L;
      given(memberRepository.findById(nonExistentMemberId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(
              () -> memberUpdateService.updateMemberInfo(nonExistentMemberId, "새닉네임", null))
          .isInstanceOf(CustomException.class)
          .hasMessageContaining(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("닉네임이 중복되면 ALREADY_EXISTS 예외를 던진다.")
    void updateNickname_duplicateNickname_throwsException() {
      // given
      String duplicatedNickname = "중복닉네임";
      given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
      given(memberRepository.existsByNicknameAndIdNot(duplicatedNickname, member.getId()))
          .willReturn(true);

      // when & then
      assertThatThrownBy(
              () -> memberUpdateService.updateMemberInfo(memberId, duplicatedNickname, null))
          .isInstanceOf(CustomException.class)
          .hasMessageContaining(MemberErrorCode.ALREADY_EXISTS.getMessage());
    }

    @Test
    @DisplayName("닉네임 형식이 잘못되면 NICKNAME_INVALID_FORMAT 예외를 던진다.")
    void updateNickname_invalidFormat_throwsException() {
      // given
      String invalidNickname = "Invalid!!@#";
      given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

      // when & then
      assertThatThrownBy(() -> memberUpdateService.updateMemberInfo(memberId, invalidNickname, null))
          .isInstanceOf(CustomException.class)
          .hasMessageContaining(MemberErrorCode.NICKNAME_INVALID_FORMAT.getMessage());
    }

    @Test
    @DisplayName("닉네임과 이미지가 모두 변경되지 않으면 NO_CHANGES 예외를 던진다.")
    void updateNothing_throwsNoChangeException() {
      // given
      given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

      // when & then
      assertThatThrownBy(
              () ->
                  memberUpdateService.updateMemberInfo(
                      memberId, member.getNickname(), member.getImageUrl()))
          .isInstanceOf(CustomException.class)
          .hasMessageContaining(MemberErrorCode.NO_CHANGES.getMessage());
    }

    @Test
    @DisplayName("예상치 못한 예외가 발생하면 NICKNAME_UPDATE_FAILED 예외를 던진다.")
    void unexpectedException_throwsUpdateFailedException() {
      // given
      String newNickname = "정상닉네임";
      given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
      given(memberRepository.existsByNicknameAndIdNot(any(), any()))
          .willThrow(new RuntimeException("DB 오류"));

      // when & then
      assertThatThrownBy(() -> memberUpdateService.updateMemberInfo(memberId, newNickname, null))
          .isInstanceOf(CustomException.class)
          .hasMessageContaining(MemberErrorCode.NICKNAME_UPDATE_FAILED.getMessage());
    }
  }
}
