package ktb.leafresh.backend.domain.challenge.group.infrastructure.dto;

public record AiChallengeValidationResponseDto(
        boolean result // true = 생성 가능, false = 유사 챌린지 존재
) {}
