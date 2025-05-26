package ktb.leafresh.backend.domain.member.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BadgeResponseDto {
    private Long id;
    private String name;
    private String condition;
    private String imageUrl;
    @JsonProperty("isLocked")
    private boolean isLocked;

    public static BadgeResponseDto of(Badge badge, boolean isLocked, String lockImageUrl) {
        return BadgeResponseDto.builder()
                .id(badge.getId())
                .name(badge.getName())
                .condition(badge.getCondition())
                .imageUrl(isLocked ? lockImageUrl : badge.getImageUrl())
                .isLocked(isLocked)
                .build();
    }
}
