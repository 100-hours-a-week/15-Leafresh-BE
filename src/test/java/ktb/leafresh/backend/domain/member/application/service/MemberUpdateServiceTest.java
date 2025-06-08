package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.MemberUpdateResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static ktb.leafresh.backend.support.fixture.MemberFixture.of;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberUpdateServiceTest {

    private MemberRepository memberRepository;
    private MemberUpdateService service;

    private Member member;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        service = new MemberUpdateService(memberRepository);
        member = of(1L, "test@leafresh.com", "기존닉네임");
    }

    @Test
    @DisplayName("닉네임과 이미지 URL이 정상적으로 변경된다")
    void update_success_both() {
        // given
        String newNickname = "새닉네임";
        String newImageUrl = "https://dummy.image/new.png";

        when(memberRepository.existsByNicknameAndIdNot(newNickname, member.getId()))
                .thenReturn(false);

        // when
        MemberUpdateResponseDto result = service.updateMemberInfo(member, newNickname, newImageUrl);

        // then
        assertThat(result.getNickname()).isEqualTo(newNickname);
        assertThat(result.getImageUrl()).isEqualTo(newImageUrl);
    }

    @Test
    @DisplayName("닉네임만 변경된다")
    void update_success_nickname_only() {
        String newNickname = "변경닉네임";
        when(memberRepository.existsByNicknameAndIdNot(newNickname, member.getId())).thenReturn(false);

        MemberUpdateResponseDto result = service.updateMemberInfo(member, newNickname, member.getImageUrl());

        assertThat(result.getNickname()).isEqualTo(newNickname);
        assertThat(result.getImageUrl()).isEqualTo(member.getImageUrl());
    }

    @Test
    @DisplayName("이미지 URL만 변경된다")
    void update_success_image_only() {
        String newImageUrl = "https://dummy.image/updated.png";

        MemberUpdateResponseDto result = service.updateMemberInfo(member, member.getNickname(), newImageUrl);

        assertThat(result.getNickname()).isEqualTo(member.getNickname());
        assertThat(result.getImageUrl()).isEqualTo(newImageUrl);
    }

    @Test
    @DisplayName("변경사항이 없으면 예외 발생")
    void update_fail_no_changes() {
        CustomException exception = catchThrowableOfType(
                () -> service.updateMemberInfo(member, member.getNickname(), member.getImageUrl()),
                CustomException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.NO_CHANGES);
    }

    @Test
    @DisplayName("중복된 닉네임이면 예외 발생")
    void update_fail_duplicated_nickname() {
        String duplicateNickname = "중복닉네임";
        when(memberRepository.existsByNicknameAndIdNot(duplicateNickname, member.getId()))
                .thenReturn(true);

        CustomException exception = catchThrowableOfType(
                () -> service.updateMemberInfo(member, duplicateNickname, member.getImageUrl()),
                CustomException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.ALREADY_EXISTS);
    }

    @Test
    @DisplayName("닉네임 형식이 유효하지 않으면 예외 발생")
    void update_fail_invalid_nickname_format() {
        String invalidNickname = "🔥Invalid닉네임!";

        CustomException exception = catchThrowableOfType(
                () -> service.updateMemberInfo(member, invalidNickname, member.getImageUrl()),
                CustomException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.NICKNAME_INVALID_FORMAT);
    }

    @Test
    @DisplayName("서버 내부 오류 발생 시 NICKNAME_UPDATE_FAILED 예외 반환")
    void update_fail_unexpected_exception() {
        String newNickname = "예외유발";
        doThrow(new RuntimeException("DB 오류"))
                .when(memberRepository).existsByNicknameAndIdNot(newNickname, member.getId());

        CustomException exception = catchThrowableOfType(
                () -> service.updateMemberInfo(member, newNickname, member.getImageUrl()),
                CustomException.class
        );

        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.NICKNAME_UPDATE_FAILED);
    }
}
