package ktb.leafresh.backend.domain.member.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberUpdateResponseDto {
    private final String nickname;
    private final String imageUrl;
}
