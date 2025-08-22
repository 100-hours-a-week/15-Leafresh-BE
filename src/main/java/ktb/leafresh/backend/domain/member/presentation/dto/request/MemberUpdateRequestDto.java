package ktb.leafresh.backend.domain.member.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ktb.leafresh.backend.global.validator.ValidImageUrl;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "회원 정보 수정 요청 DTO")
@Getter
@NoArgsConstructor
public class MemberUpdateRequestDto {

  @Schema(description = "닉네임", example = "리프레시유저", minLength = 1, maxLength = 20)
  @Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하로 입력해주세요.")
  @Pattern(
      regexp = "^[a-zA-Z0-9가-힣]{1,20}$",
      message = "닉네임은 특수문자 없이 1~20자의 영문, 숫자, 한글만 사용할 수 있습니다.")
  private String nickname;

  @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
  @ValidImageUrl
  private String imageUrl;
}
