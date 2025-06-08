package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.MemberInfoResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static ktb.leafresh.backend.support.fixture.MemberFixture.of;
import static ktb.leafresh.backend.support.fixture.TreeLevelFixture.defaultLevel;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberInfoQueryServiceTest {

    private MemberRepository memberRepository;
    private MemberInfoQueryService service;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        service = new MemberInfoQueryService(memberRepository);
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    void getMemberInfo_success() {
        // given
        Long memberId = 1L;
        TreeLevel treeLevel = defaultLevel();
        Member member = of(memberId, "test@leafresh.com", "테스터", treeLevel); // treeLevel 주입

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        MemberInfoResponseDto result = service.getMemberInfo(memberId);

        // then
        assertThat(result.getNickname()).isEqualTo("테스터");
        assertThat(result.getTreeLevelId()).isEqualTo(treeLevel.getId());
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID는 예외 발생")
    void getMemberInfo_memberNotFound() {
        // given
        Long memberId = 1L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when
        CustomException exception = catchThrowableOfType(
                () -> service.getMemberInfo(memberId),
                CustomException.class
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("회원에 트리 레벨이 없으면 예외 발생")
    void getMemberInfo_noTreeLevel() {
        // given
        Long memberId = 1L;
        Member member = of(memberId, "test@leafresh.com", "테스터", null); // null 전달

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        CustomException exception = catchThrowableOfType(
                () -> service.getMemberInfo(memberId),
                CustomException.class
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.TREE_LEVEL_NOT_FOUND);
    }

    @Test
    @DisplayName("알 수 없는 예외가 발생하면 MEMBER_INFO_QUERY_FAILED 반환")
    void getMemberInfo_internalError() {
        // given
        Long memberId = 1L;
        when(memberRepository.findById(memberId)).thenThrow(new RuntimeException("DB 오류"));

        // when
        CustomException exception = catchThrowableOfType(
                () -> service.getMemberInfo(memberId),
                CustomException.class
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_INFO_QUERY_FAILED);
    }
}
