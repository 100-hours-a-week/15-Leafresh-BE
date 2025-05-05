package ktb.leafresh.backend.domain.challenge.personal.application.service;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.challenge.personal.infrastructure.repository.PersonalChallengeRepository;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response.*;
import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalChallengeReadService {

    private final PersonalChallengeRepository repository;
    private final PersonalChallengeVerificationRepository verificationRepository;

    public PersonalChallengeListResponseDto getByDayOfWeek(DayOfWeek dayOfWeek) {
        List<PersonalChallenge> challenges = repository.findAllByDayOfWeek(dayOfWeek);

        if (challenges.isEmpty()) {
            throw new CustomException(ChallengeErrorCode.PERSONAL_CHALLENGE_EMPTY);
        }

        return new PersonalChallengeListResponseDto(PersonalChallengeSummaryDto.fromEntities(challenges));
    }

    public PersonalChallengeDetailResponseDto getChallengeDetail(Long memberIdOrNull, Long challengeId) {
        PersonalChallenge challenge = repository.findById(challengeId)
                .orElseThrow(() -> new CustomException(ChallengeErrorCode.PERSONAL_CHALLENGE_NOT_FOUND));

        List<PersonalChallengeExampleImageDto> exampleImages = challenge.getExampleImages().stream()
                .map(PersonalChallengeExampleImageDto::from)
                .toList();

        ChallengeStatus status = resolveChallengeStatus(memberIdOrNull, challengeId);

        return PersonalChallengeDetailResponseDto.of(challenge, exampleImages, status);
    }

    private ChallengeStatus resolveChallengeStatus(Long memberIdOrNull, Long challengeId) {
        if (memberIdOrNull == null) {
            log.info("비회원 접근 - 인증 상태 조회 생략");
            return ChallengeStatus.NOT_SUBMITTED;
        }

        // 1. 챌린지 가져오기
        PersonalChallenge challenge = repository.findById(challengeId)
                .orElseThrow(() -> new CustomException(ChallengeErrorCode.PERSONAL_CHALLENGE_NOT_FOUND));

        // 2. 요일 계산
        java.time.DayOfWeek javaDayOfWeek = java.time.DayOfWeek.valueOf(challenge.getDayOfWeek().name()); // enum 변환
        java.time.LocalDate challengeDate = java.time.LocalDate.now()
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(javaDayOfWeek));

        LocalDateTime startOfDay = challengeDate.atStartOfDay();
        LocalDateTime endOfDay = challengeDate.atTime(LocalTime.MAX);

        // 3. 인증 여부 확인
        return verificationRepository
                .findTopByMemberIdAndPersonalChallengeIdAndCreatedAtBetween(memberIdOrNull, challengeId, startOfDay, endOfDay)
                .map(PersonalChallengeVerification::getStatus)
                .orElse(ChallengeStatus.NOT_SUBMITTED);
    }

    public PersonalChallengeRuleResponseDto getChallengeRules(Long challengeId) {
        PersonalChallenge challenge = repository.findById(challengeId)
                .orElseThrow(() -> new CustomException(ChallengeErrorCode.PERSONAL_CHALLENGE_NOT_FOUND));

        List<PersonalChallengeExampleImageDto> exampleImages = challenge.getExampleImages().stream()
                .map(PersonalChallengeExampleImageDto::from)
                .toList();

        return PersonalChallengeRuleResponseDto.of(challenge, exampleImages);
    }
}
