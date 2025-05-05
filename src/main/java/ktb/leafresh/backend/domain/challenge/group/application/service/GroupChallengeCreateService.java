package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.application.factory.GroupChallengeExampleImageAssembler;
import ktb.leafresh.backend.domain.challenge.group.application.factory.GroupChallengeFactory;
import ktb.leafresh.backend.domain.challenge.group.application.validator.AiChallengePolicyValidator;
import ktb.leafresh.backend.domain.challenge.group.application.validator.GroupChallengeDomainValidator;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.*;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeCreateRequestDto;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.GroupChallengeCreateResponseDto;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupChallengeCreateService {

    private final MemberRepository memberRepository;
    private final GroupChallengeDomainValidator domainValidator;
    private final AiChallengePolicyValidator aiValidator;
    private final GroupChallengeFactory factory;
    private final GroupChallengeExampleImageAssembler assembler;
    private final GroupChallengeRepository groupChallengeRepository;

    @Transactional
    public GroupChallengeCreateResponseDto create(Long memberId, GroupChallengeCreateRequestDto dto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        domainValidator.validate(dto);
        aiValidator.validate(memberId, dto);

        GroupChallenge challenge = factory.create(dto, member);
        assembler.assemble(challenge, dto);

        groupChallengeRepository.save(challenge);
        return new GroupChallengeCreateResponseDto(challenge.getId());
    }
}
