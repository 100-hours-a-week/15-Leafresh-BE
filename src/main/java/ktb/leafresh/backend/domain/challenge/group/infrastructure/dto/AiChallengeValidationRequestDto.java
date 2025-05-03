package ktb.leafresh.backend.domain.challenge.group.infrastructure.dto;

public record AiChallengeValidationRequestDto(
        Long memberId,
        String challengeName,
        String startDate,
        String endDate
) {}
