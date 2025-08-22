package ktb.leafresh.backend.global.security;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "JWT 토큰 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDto {

  @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  private String accessToken;

  @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  private String refreshToken;

  @Schema(description = "액세스 토큰 만료 시간 (초)", example = "3600")
  private Long accessTokenExpiresIn;
}
