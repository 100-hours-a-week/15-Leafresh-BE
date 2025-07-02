package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.MemberInfoResponseDto;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MemberInfoQueryServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberInfoQueryService memberInfoQueryService;

    private Member member;
    private TreeLevel treeLevel;

    @BeforeEach
    void setUp() {
        treeLevel = TreeLevelFixture.defaultLevel();
        member = MemberFixture.of();
    }

    @Nested
    @DisplayName("getMemberInfo")
    class GetMemberInfo {

        @Test
        @DisplayName("정상적으로 회원 정보를 조회한다.")
        void getMemberInfo_withValidId_returnsResponseDto() {
            // given
            given(memberRepository.findById(anyLong())).willReturn(Optional.of(member));

            // when
            MemberInfoResponseDto result = memberInfoQueryService.getMemberInfo(1L);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getNickname()).isEqualTo(member.getNickname());
            assertThat(result.getEmail()).isEqualTo(member.getEmail());
            assertThat(result.getProfileImageUrl()).isEqualTo(member.getImageUrl());
            assertThat(result.getTreeLevelName()).isEqualTo(member.getTreeLevel().getName().name());
        }

        @Test
        @DisplayName("존재하지 않는 회원 ID일 경우 예외를 던진다.")
        void getMemberInfo_withInvalidId_throwsCustomException() {
            // given
            given(memberRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> memberInfoQueryService.getMemberInfo(999L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("TreeLevel이 null이면 예외를 던진다.")
        void getMemberInfo_withNullTreeLevel_throwsCustomException() {
            // given
            Member memberWithoutTreeLevel = MemberFixture.of();
            ReflectionTestUtils.setField(memberWithoutTreeLevel, "treeLevel", null);
            given(memberRepository.findById(anyLong())).willReturn(Optional.of(memberWithoutTreeLevel));

            // when & then
            assertThatThrownBy(() -> memberInfoQueryService.getMemberInfo(1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(MemberErrorCode.TREE_LEVEL_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("예상치 못한 예외가 발생하면 MEMBER_INFO_QUERY_FAILED 예외를 던진다.")
        void getMemberInfo_withUnexpectedException_throwsGenericCustomException() {
            // given
            given(memberRepository.findById(anyLong())).willThrow(new RuntimeException("DB 연결 실패"));

            // when & then
            assertThatThrownBy(() -> memberInfoQueryService.getMemberInfo(1L))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(MemberErrorCode.MEMBER_INFO_QUERY_FAILED.getMessage());
        }
    }
}
