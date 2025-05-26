package ktb.leafresh.backend.domain.member.presentation.dto.response;

import ktb.leafresh.backend.domain.member.domain.entity.enums.BadgeType;
import lombok.Builder;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class BadgeListResponseDto {

    private final Map<String, List<BadgeResponseDto>> badges;

    public static BadgeListResponseDto from(Map<BadgeType, List<BadgeResponseDto>> grouped) {
        // 순서 보장 위해 LinkedHashMap 사용
        Map<String, List<BadgeResponseDto>> result = new LinkedHashMap<>();

        // 순서 명시
        List<BadgeType> order = List.of(
                BadgeType.GROUP,
                BadgeType.PERSONAL,
                BadgeType.TOTAL,
                BadgeType.SPECIAL,
                BadgeType.EVENT
        );

        for (BadgeType type : order) {
            if (grouped.containsKey(type)) {
                result.put(type.name().toLowerCase(), grouped.get(type));
            }
        }

        return BadgeListResponseDto.builder()
                .badges(result)
                .build();
    }
}
