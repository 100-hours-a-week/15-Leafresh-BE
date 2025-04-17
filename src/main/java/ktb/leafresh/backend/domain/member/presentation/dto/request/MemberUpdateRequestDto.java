package ktb.leafresh.backend.domain.member.presentation.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ktb.leafresh.backend.global.validator.ValidGcsImageUrl;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberUpdateRequestDto {

        @Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하로 입력해주세요.")
        @Pattern(
                regexp = "^[a-zA-Z0-9가-힣]{1,20}$",
                message = "닉네임은 특수문자 없이 1~20자의 영문, 숫자, 한글만 사용할 수 있습니다."
        )
        private String nickname;

        @ValidGcsImageUrl
        private String imageUrl;
}
