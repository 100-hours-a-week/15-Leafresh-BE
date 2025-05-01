package ktb.leafresh.backend.domain.auth.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import ktb.leafresh.backend.domain.member.domain.entity.enums.LoginType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "OAuth 회원가입 요청")
public record OAuthSignupRequestDto(

        @NotBlank(message = "이메일은 필수입니다.")
        @Schema(description = "OAuth 이메일", example = "test@example.com")
        String email,

        @NotNull(message = "OAuth 제공자 정보는 필수입니다.")
        @Schema(description = "OAuth 제공자 정보(name: 제공자, id: 고유 ID)")
        Provider provider,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Schema(description = "회원 닉네임", example = "leafresh")
        String nickname,

        @NotBlank(message = "프로필 이미지 URL은 필수입니다.")
        @Schema(description = "OAuth 프로필 이미지 URL", example = "https://k.kakaocdn.net/.../profile.jpg")
        String imageUrl

) {
        public record Provider(
                @NotNull(message = "OAuth 제공자 이름은 필수입니다.")
                @Schema(description = "OAuth 제공자 이름", example = "KAKAO")
                LoginType name,

                @NotBlank(message = "providerId는 필수입니다.")
                @Schema(description = "OAuth 제공자에서 발급한 고유 ID", example = "1234567890")
                String id
        ) {}
}
