package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.MemberInfoResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static ktb.leafresh.backend.support.fixture.MemberFixture.of;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberInfoQueryServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberInfoQueryService service;

    @Test
    @DisplayName("회원 정보 조회 성공")
    void getMemberInfo_정상입력_회원정보조회성공() {
        // given
        Member member = of("tester@leafresh.com", "테스터");
        TreeLevel treeLevel = member.getTreeLevel();
        given(memberRepository.findById(any())).willReturn(Optional.of(member));

        // when
        MemberInfoResponseDto result = service.getMemberInfo(1L);

        // then
        assertThat(result.getEmail()).isEqualTo(member.getEmail());
        assertThat(result.getNickname()).isEqualTo(member.getNickname());
        assertThat(result.getTreeLevelId()).isEqualTo(treeLevel.getId());
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID는 예외 발생")
    void getMemberInfo_회원없음_MemberNotFound예외발생() {
        // given
        given(memberRepository.findById(any())).willReturn(Optional.empty());

        // when
        CustomException exception = catchThrowableOfType(
                () -> service.getMemberInfo(1L),
                CustomException.class
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_NOT_FOUND);
    }


    @Test
    @DisplayName("트리 레벨이 없는 회원은 예외 발생")
    void getMemberInfo_트리레벨없음_TreeLevelNotFound예외발생() {
        // given
        Member member = of("tester@leafresh.com", "테스터");
        ReflectionTestUtils.setField(member, "treeLevel", null);
        given(memberRepository.findById(any())).willReturn(Optional.of(member));

        // when
        CustomException exception = catchThrowableOfType(
                () -> service.getMemberInfo(1L),
                CustomException.class
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.TREE_LEVEL_NOT_FOUND);
    }

    @Test
    @DisplayName("서비스 내부 오류 발생 시 MEMBER_INFO_QUERY_FAILED 예외 발생")
    void getMemberInfo_예상치못한예외_INTERNAL_SERVER_ERROR() {
        // given
        given(memberRepository.findById(any())).willThrow(new RuntimeException("DB 오류"));

        // when
        CustomException exception = catchThrowableOfType(
                () -> service.getMemberInfo(1L),
                CustomException.class
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(MemberErrorCode.MEMBER_INFO_QUERY_FAILED);
    }
}
