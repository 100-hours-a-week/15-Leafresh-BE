package ktb.leafresh.backend.domain.challenge.group.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GroupChallengeCategoryResponseDto {
    private String category;
    private String label;
    private String imageUrl;
}
