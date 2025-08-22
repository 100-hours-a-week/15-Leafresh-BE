package ktb.leafresh.backend.domain.member.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "회원 정보 수정 응답 DTO")
@Getter
@Builder
public class MemberUpdateResponseDto {

  @Schema(description = "수정된 닉네임", example = "리프레시유저")
  private final String nickname;

  @Schema(description = "수정된 프로필 이미지 URL", example = "https://example.com/profile.jpg")
  private final String imageUrl;
}
