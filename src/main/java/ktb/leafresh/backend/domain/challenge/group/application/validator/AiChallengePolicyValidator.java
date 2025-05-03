package ktb.leafresh.backend.domain.challenge.group.application.validator;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.client.AiChallengeValidationClient;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.AiChallengeValidationRequestDto;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.AiChallengeValidationRequestDto.ChallengeSummary;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.dto.AiChallengeValidationResponseDto;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeCreateRequestDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AiChallengePolicyValidator {

    private final AiChallengeValidationClient aiChallengeValidationClient;
    private final GroupChallengeRepository groupChallengeRepository;

    public void validate(Long memberId, GroupChallengeCreateRequestDto dto) {
        // 현재 및 예정 챌린지 목록 조회
        List<GroupChallenge> activeChallenges = groupChallengeRepository.findAllValidAndOngoing(LocalDateTime.now());

        // 챌린지 목록을 DTO 형태로 변환
        List<ChallengeSummary> challengeSummaries = activeChallenges.stream()
                .map(ch -> new ChallengeSummary(
                        ch.getId(),
                        ch.getTitle(),
                        ch.getStartDate().toString(),
                        ch.getEndDate().toString()
                ))
                .toList();

        // AI 요청 생성
        AiChallengeValidationRequestDto aiRequest = new AiChallengeValidationRequestDto(
                memberId,
                dto.title(),
                dto.startDate().toString(),
                dto.endDate().toString(),
                challengeSummaries
        );

        // AI 서버에 유사 챌린지 존재 여부 요청
        AiChallengeValidationResponseDto aiResponse = aiChallengeValidationClient.validateChallenge(aiRequest);

        if (!aiResponse.result()) {
            throw new CustomException(ErrorCode.CHALLENGE_CREATION_REJECTED_BY_AI);
        }
    }
}
