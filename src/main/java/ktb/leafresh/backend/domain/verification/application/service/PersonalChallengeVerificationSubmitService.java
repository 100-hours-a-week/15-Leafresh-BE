package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.challenge.personal.infrastructure.repository.PersonalChallengeRepository;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;
import ktb.leafresh.backend.domain.verification.domain.event.VerificationCreatedEvent;
import ktb.leafresh.backend.domain.verification.infrastructure.dto.request.AiVerificationRequestDto;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.PersonalChallengeVerificationRequestDto;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeType;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PersonalChallengeVerificationSubmitService {

    private final MemberRepository memberRepository;
    private final PersonalChallengeRepository personalChallengeRepository;
    private final PersonalChallengeVerificationRepository verificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void submit(Long memberId, Long challengeId, PersonalChallengeVerificationRequestDto dto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        PersonalChallenge challenge = personalChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new CustomException(ChallengeErrorCode.PERSONAL_CHALLENGE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        boolean alreadySubmitted = verificationRepository
                .findTopByMemberIdAndPersonalChallengeIdAndCreatedAtBetween(
                        memberId, challengeId, now.toLocalDate().atStartOfDay(), now.toLocalDate().atTime(23, 59, 59)
                )
                .isPresent();

        if (alreadySubmitted) {
            throw new CustomException(VerificationErrorCode.ALREADY_SUBMITTED);
        }

        PersonalChallengeVerification verification = PersonalChallengeVerification.builder()
                .member(member)
                .personalChallenge(challenge)
                .imageUrl(dto.imageUrl())
                .content(dto.content())
                .submittedAt(now)
                .status(ChallengeStatus.PENDING_APPROVAL)
                .build();

        verificationRepository.save(verification);

        // 이벤트 발행 (커밋 후 실행됨)
        AiVerificationRequestDto aiRequest = AiVerificationRequestDto.builder()
                .verificationId(verification.getId())
                .type(ChallengeType.PERSONAL)
                .imageUrl(dto.imageUrl())
                .memberId(memberId)
                .challengeId(challengeId)
                .date(now.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .challengeName(challenge.getTitle())
                .build();

        eventPublisher.publishEvent(new VerificationCreatedEvent(aiRequest));
    }
}
