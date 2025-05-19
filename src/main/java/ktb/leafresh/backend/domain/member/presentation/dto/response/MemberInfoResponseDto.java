package ktb.leafresh.backend.domain.member.presentation.dto.response;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MemberInfoResponseDto {

    private String nickname;
    private String profileImageUrl;
    private Long treeLevelId;
    private String treeLevelName;
    private String treeImageUrl;

    public static MemberInfoResponseDto of(Member member, TreeLevel treeLevel) {
        return MemberInfoResponseDto.builder()
                .nickname(member.getNickname())
                .profileImageUrl(member.getImageUrl())
                .treeLevelId(treeLevel.getId())
                .treeLevelName(treeLevel.getName().name())
                .treeImageUrl(treeLevel.getImageUrl())
                .build();
    }
}
