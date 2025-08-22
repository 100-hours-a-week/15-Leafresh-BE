package ktb.leafresh.backend.domain.member.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 중복 확인 응답 DTO")
public record NicknameCheckResponseDto(
    @Schema(description = "닉네임 중복 여부", example = "false") boolean isDuplicated) {}
