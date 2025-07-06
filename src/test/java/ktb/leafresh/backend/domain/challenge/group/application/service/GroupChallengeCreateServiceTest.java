package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.application.factory.GroupChallengeExampleImageAssembler;
import ktb.leafresh.backend.domain.challenge.group.application.factory.GroupChallengeFactory;
import ktb.leafresh.backend.domain.challenge.group.application.validator.AiChallengePolicyValidator;
import ktb.leafresh.backend.domain.challenge.group.application.validator.GroupChallengeDomainValidator;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeCreateRequestDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeCreateResponseDto;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupChallengeCreateService 테스트")
class GroupChallengeCreateServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GroupChallengeDomainValidator domainValidator;

    @Mock
    private AiChallengePolicyValidator aiValidator;

    @Mock
    private GroupChallengeFactory factory;

    @Mock
    private GroupChallengeExampleImageAssembler assembler;

    @Mock
    private GroupChallengeRepository groupChallengeRepository;

    @InjectMocks private GroupChallengeCreateService createService;

    private Member member;
    private GroupChallenge challenge;
    private GroupChallengeCreateRequestDto request;

    @BeforeEach
    void setUp() {
        member = MemberFixture.of();
        challenge = GroupChallengeFixture.of(member, GroupChallengeCategoryFixture.defaultCategory());
        request = mock(GroupChallengeCreateRequestDto.class);
    }

    @Test
    @DisplayName("단체 챌린지 생성 성공 시 ID 반환")
    void createGroupChallenge_withValidInput_returnsResponse() {
        // given
        Long memberId = 1L;

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
        willDoNothing().given(domainValidator).validate(request);
        willDoNothing().given(aiValidator).validate(memberId, request);
        given(factory.create(request, member)).willReturn(challenge);
        willDoNothing().given(assembler).assemble(challenge, request);
        given(groupChallengeRepository.save(challenge)).willReturn(challenge);

        // when
        GroupChallengeCreateResponseDto response = createService.create(memberId, request);

        // then
        assertThat(response.id()).isEqualTo(challenge.getId());
    }

    @Test
    @DisplayName("존재하지 않는 회원이면 예외 발생")
    void createGroupChallenge_withInvalidMember_throwsException() {
        // given
        Long invalidMemberId = 999L;
        given(memberRepository.findById(invalidMemberId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> createService.create(invalidMemberId, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
    }
}
