package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.domain.entity.enums.TreeLevelName;

import java.util.ArrayList;
import java.util.List;

public class TreeLevelFixture {

  private static final TreeLevelName DEFAULT_NAME = TreeLevelName.SPROUT;
  private static final int DEFAULT_MIN_LEAF_POINT = 0;
  private static final String DEFAULT_IMAGE_URL = "https://dummy.image/tree/sprout.png";
  private static final String DEFAULT_DESCRIPTION = "기본 트리 레벨";

  /** ID 없이 기본 TreeLevel 생성 (Best Practice: ID 지양) */
  public static TreeLevel defaultLevel() {
    return TreeLevel.builder()
        .name(DEFAULT_NAME)
        .minLeafPoint(DEFAULT_MIN_LEAF_POINT)
        .imageUrl(DEFAULT_IMAGE_URL)
        .description(DEFAULT_DESCRIPTION)
        .members(new ArrayList<>())
        .build();
  }

  /** 다른 TreeLevelName으로 레벨을 생성하고 싶을 때 사용 */
  public static TreeLevel of(TreeLevelName name) {
    return TreeLevel.builder()
        .name(name)
        .minLeafPoint(DEFAULT_MIN_LEAF_POINT)
        .imageUrl("https://dummy.image/tree/" + name.name().toLowerCase() + ".png")
        .description(name + " 단계")
        .members(List.of())
        .build();
  }
}
