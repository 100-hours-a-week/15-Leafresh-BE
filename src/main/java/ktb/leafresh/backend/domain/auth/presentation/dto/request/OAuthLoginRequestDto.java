package ktb.leafresh.backend.domain.auth.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "OAuth 로그인 요청")
public record OAuthLoginRequestDto(
    @Schema(description = "OAuth 인가 코드", example = "abc123") @NotBlank(message = "인가 코드는 필수입니다.")
        String authorizationCode) {}
