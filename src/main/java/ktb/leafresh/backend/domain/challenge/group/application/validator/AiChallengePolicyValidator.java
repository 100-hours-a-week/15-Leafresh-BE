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

    // 1. 필수 항목 수동 검증
    if (memberId == null) {
      throw new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_VALIDATION_MISSING_MEMBER_ID);
    }
    if (dto.title() == null || dto.title().isBlank()) {
      throw new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_VALIDATION_MISSING_TITLE);
    }
    if (dto.startDate() == null) {
      throw new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_VALIDATION_MISSING_START_DATE);
    }
    if (dto.endDate() == null) {
      throw new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_VALIDATION_MISSING_END_DATE);
    }

    try {
      // 2. 현재 및 예정 챌린지 조회
      List<GroupChallenge> activeChallenges =
          groupChallengeRepository.findAllValidAndOngoing(LocalDateTime.now());
      log.debug("[AI 정책 검증] 조회된 유효 챌린지 개수 = {}", activeChallenges.size());

      // 3. 요약 형태로 변환
      List<ChallengeSummary> challengeSummaries =
          activeChallenges.stream()
              .map(
                  ch ->
                      new ChallengeSummary(
                          ch.getId(),
                          ch.getTitle(),
                          ch.getStartDate().toString(),
                          ch.getEndDate().toString()))
              .toList();
      log.debug("[AI 정책 검증] 요약된 챌린지 목록: {}", challengeSummaries);

      // 4. AI 요청 DTO 생성
      AiChallengeValidationRequestDto aiRequest =
          new AiChallengeValidationRequestDto(
              memberId,
              dto.title(),
              dto.startDate().toString(),
              dto.endDate().toString(),
              challengeSummaries);
      log.info("[AI 정책 검증] 생성된 AI 요청 DTO: {}", aiRequest);

      // 5. AI 서버 호출
      AiChallengeValidationResponseDto aiResponse =
          aiChallengeValidationClient.validateChallenge(aiRequest);
      log.info("[AI 정책 검증] AI 응답 결과: {}", aiResponse);

      if (!aiResponse.result()) {
        log.warn("[AI 정책 검증] 챌린지 생성 거부됨 - AI 응답: {}", aiResponse);
        throw new CustomException(ChallengeErrorCode.CHALLENGE_CREATION_REJECTED_BY_AI);
      }

      log.info("[AI 정책 검증] 통과");

    } catch (IllegalArgumentException e) {
      log.error("[AI 정책 검증] 형식 오류", e);
      throw new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_VALIDATION_INVALID_FORMAT);
    } catch (Exception e) {
      log.error("[AI 정책 검증] AI 유사도 분석 실패", e);
      throw new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_VALIDATION_PROCESSING_FAILED);
    }
  }
}
