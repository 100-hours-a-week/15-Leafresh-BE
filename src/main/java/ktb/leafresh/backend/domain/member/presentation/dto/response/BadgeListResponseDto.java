package ktb.leafresh.backend.domain.member.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.member.domain.entity.enums.BadgeType;
import lombok.Builder;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Schema(description = "배지 목록 응답 DTO")
@Getter
@Builder
public class BadgeListResponseDto {

  @Schema(
      description = "배지 타입별 배지 목록",
      example =
          "{ \"group\": [...], \"personal\": [...], \"total\": [...], \"special\": [...], \"event\": [...] }")
  private final Map<String, List<BadgeResponseDto>> badges;

  public static BadgeListResponseDto from(Map<BadgeType, List<BadgeResponseDto>> grouped) {
    // 순서 보장 위해 LinkedHashMap 사용
    Map<String, List<BadgeResponseDto>> result = new LinkedHashMap<>();

    // 순서 명시
    List<BadgeType> order =
        List.of(
            BadgeType.GROUP,
            BadgeType.PERSONAL,
            BadgeType.TOTAL,
            BadgeType.SPECIAL,
            BadgeType.EVENT);

    for (BadgeType type : order) {
      if (grouped.containsKey(type)) {
        result.put(type.name().toLowerCase(), grouped.get(type));
      }
    }

    return BadgeListResponseDto.builder().badges(result).build();
  }
}
