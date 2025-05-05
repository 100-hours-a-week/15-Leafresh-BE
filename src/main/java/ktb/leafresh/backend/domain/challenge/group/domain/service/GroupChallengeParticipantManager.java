package ktb.leafresh.backend.domain.challenge.group.domain.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.domain.support.validator.GroupChallengeParticipationValidator;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeParticipantRecordRepository;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.global.common.entity.enums.ParticipantStatus;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupChallengeParticipantManager {

    private final GroupChallengeRepository groupChallengeRepository;
    private final GroupChallengeParticipantRecordRepository participantRepository;
    private final MemberRepository memberRepository;
    private final GroupChallengeParticipationValidator validator;

    public Long participate(Long memberId, Long challengeId) {
        Member member = findMember(memberId);
        GroupChallenge challenge = findChallenge(challengeId);

        validator.validateNotAlreadyParticipated(challengeId, memberId);

        boolean isFull = challenge.isFull();
        ParticipantStatus status = isFull ? ParticipantStatus.WAITING : ParticipantStatus.ACTIVE;

        if (status == ParticipantStatus.ACTIVE) {
            challenge.increaseParticipantCount();
        }

        GroupChallengeParticipantRecord record = GroupChallengeParticipantRecord.create(member, challenge, status);
        participantRepository.save(record);

        return record.getId();
    }

    public void drop(Long memberId, Long challengeId) {
        GroupChallengeParticipantRecord record = participantRepository
                .findByGroupChallengeIdAndMemberIdAndDeletedAtIsNull(challengeId, memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHALLENGE_ALREADY_DELETED));

        validator.validateDroppable(record);

        GroupChallenge challenge = record.getGroupChallenge();

        if (record.isActive()) {
            challenge.decreaseParticipantCount();
        }

        record.changeStatus(ParticipantStatus.DROPPED);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private GroupChallenge findChallenge(Long challengeId) {
        return groupChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_CHALLENGE_NOT_FOUND));
    }
}
