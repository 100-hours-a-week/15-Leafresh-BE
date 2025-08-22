package ktb.leafresh.backend.domain.member.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "회원 정보 응답 DTO")
@Getter
@Builder
@AllArgsConstructor
public class MemberInfoResponseDto {

  @Schema(description = "닉네임", example = "리프레시유저")
  private String nickname;

  @Schema(description = "이메일", example = "user@example.com")
  private String email;

  @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
  private String profileImageUrl;

  @Schema(description = "나무 레벨 ID", example = "1")
  private Long treeLevelId;

  @Schema(description = "나무 레벨 이름", example = "SEEDLING")
  private String treeLevelName;

  @Schema(description = "나무 이미지 URL", example = "https://example.com/tree.jpg")
  private String treeImageUrl;

  public static MemberInfoResponseDto of(Member member, TreeLevel treeLevel) {
    return MemberInfoResponseDto.builder()
        .nickname(member.getNickname())
        .email(member.getEmail())
        .profileImageUrl(member.getImageUrl())
        .treeLevelId(treeLevel.getId())
        .treeLevelName(treeLevel.getName().name())
        .treeImageUrl(treeLevel.getImageUrl())
        .build();
  }
}
