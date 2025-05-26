package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.domain.entity.enums.TreeLevelName;

import java.util.ArrayList;

public class TreeLevelFixture {

    public static TreeLevel defaultLevel() {
        return TreeLevel.builder()
                .id(1L)
                .members(new ArrayList<>())
                .name(TreeLevelName.SPROUT)
                .minLeafPoint(0)
                .imageUrl("https://dummy.image/tree/sprout.png")
                .description("기본 트리 레벨")
                .build();
    }
}
