package ktb.leafresh.backend.domain.challenge.group.application.validator;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.client.AiChallengeValidationClient;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.request.AiChallengeValidationRequestDto;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.request.AiChallengeValidationRequestDto.ChallengeSummary;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.response.AiChallengeValidationResponseDto;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeCreateRequestDto;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiChallengePolicyValidator {

    private final AiChallengeValidationClient aiChallengeValidationClient;
    private final GroupChallengeRepository groupChallengeRepository;

    public void validate(Long memberId, GroupChallengeCreateRequestDto dto) {
        log.info("[AI 정책 검증] 시작 - memberId={}, title={}", memberId, dto.title());

        // 현재 및 예정 챌린지 목록 조회
        List<GroupChallenge> activeChallenges = groupChallengeRepository.findAllValidAndOngoing(LocalDateTime.now());
        log.debug("[AI 정책 검증] 조회된 유효 챌린지 개수 = {}", activeChallenges.size());

        // 챌린지 목록을 DTO 형태로 변환
        List<ChallengeSummary> challengeSummaries = activeChallenges.stream()
                .map(ch -> new ChallengeSummary(
                        ch.getId(),
                        ch.getTitle(),
                        ch.getStartDate().toString(),
                        ch.getEndDate().toString()
                ))
                .toList();
        log.debug("[AI 정책 검증] 요약된 챌린지 목록: {}", challengeSummaries);

        // AI 요청 생성
        AiChallengeValidationRequestDto aiRequest = new AiChallengeValidationRequestDto(
                memberId,
                dto.title(),
                dto.startDate().toString(),
                dto.endDate().toString(),
                challengeSummaries
        );
        log.info("[AI 정책 검증] 생성된 AI 요청 DTO: {}", aiRequest);

        // AI 서버에 유사 챌린지 존재 여부 요청
        AiChallengeValidationResponseDto aiResponse = aiChallengeValidationClient.validateChallenge(aiRequest);
        log.info("[AI 정책 검증] AI 응답 결과: {}", aiResponse);

        if (!aiResponse.result()) {
            log.warn("[AI 정책 검증] 챌린지 생성 거부됨 - AI 응답: {}", aiResponse);
            throw new CustomException(ChallengeErrorCode.CHALLENGE_CREATION_REJECTED_BY_AI);
        }

        log.info("[AI 정책 검증] 통과");
    }
}
