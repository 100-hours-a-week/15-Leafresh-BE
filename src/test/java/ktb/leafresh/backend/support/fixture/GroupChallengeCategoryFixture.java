package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;

import java.util.ArrayList;

public class GroupChallengeCategoryFixture {

    public static GroupChallengeCategory of(String name) {
        return GroupChallengeCategory.builder()
                .id(1L)
                .groupChallenges(new ArrayList<>())
                .name(name)
                .imageUrl("https://dummy.image/category/" + name + ".png")
                .sequenceNumber(1)
                .activated(true)
                .build();
    }
}
