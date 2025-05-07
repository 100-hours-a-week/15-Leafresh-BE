package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.challenge.personal.infrastructure.repository.PersonalChallengeRepository;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.client.AiPersonalChallengeVerificationClient;
import ktb.leafresh.backend.domain.verification.infrastructure.dto.request.AiPersonalChallengeVerificationRequestDto;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.presentation.dto.request.PersonalChallengeVerificationRequestDto;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import lombok.RequiredArgsConstructor;
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
    private final AiPersonalChallengeVerificationClient aiClient;

    @Transactional
    public void submit(Long memberId, Long challengeId, PersonalChallengeVerificationRequestDto dto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

        PersonalChallenge challenge = personalChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new CustomException(ChallengeErrorCode.PERSONAL_CHALLENGE_NOT_FOUND));

        // 오늘 이미 인증했는지 확인
        LocalDateTime now = LocalDateTime.now();
        boolean alreadySubmitted = verificationRepository
                .findTopByMemberIdAndPersonalChallengeIdAndCreatedAtBetween(
                        memberId, challengeId, now.toLocalDate().atStartOfDay(), now.toLocalDate().atTime(23, 59, 59)
                )
                .isPresent();

        if (alreadySubmitted) {
            throw new CustomException(VerificationErrorCode.ALREADY_SUBMITTED);
        }

        // 인증 생성 및 저장
        PersonalChallengeVerification verification = PersonalChallengeVerification.builder()
                .member(member)
                .personalChallenge(challenge)
                .imageUrl(dto.imageUrl())
                .content(dto.content())
                .submittedAt(now)
                .status(ChallengeStatus.PENDING_APPROVAL)
                .build();

        verificationRepository.save(verification);

        // AI 서버 요청
        AiPersonalChallengeVerificationRequestDto aiRequest = AiPersonalChallengeVerificationRequestDto.builder()
                .imageUrl(dto.imageUrl())
                .memberId(memberId)
                .challengeId(challengeId)
                .date(now.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .challengeName(challenge.getTitle())
                .build();

        aiClient.verifyImage(aiRequest);
    }
}
