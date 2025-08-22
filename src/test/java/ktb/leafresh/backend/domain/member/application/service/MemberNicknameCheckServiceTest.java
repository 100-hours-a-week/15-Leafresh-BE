package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MemberNicknameCheckServiceTest {

  @Mock private MemberRepository memberRepository;

  @InjectMocks private MemberNicknameCheckService memberNicknameCheckService;

  @Nested
  @DisplayName("isDuplicated")
  class IsDuplicated {

    @Test
    @DisplayName("닉네임이 중복된 경우 true를 반환한다.")
    void isDuplicated_withExistingNickname_returnsTrue() {
      // given
      String nickname = "테스터";
      given(memberRepository.existsByNickname(nickname)).willReturn(true);

      // when
      boolean result = memberNicknameCheckService.isDuplicated(nickname);

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("닉네임이 중복되지 않은 경우 false를 반환한다.")
    void isDuplicated_withUniqueNickname_returnsFalse() {
      // given
      String nickname = "신규닉네임";
      given(memberRepository.existsByNickname(nickname)).willReturn(false);

      // when
      boolean result = memberNicknameCheckService.isDuplicated(nickname);

      // then
      assertThat(result).isFalse();
    }
  }
}
