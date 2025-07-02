package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.enums.GroupChallengeCategoryName;

import java.util.Collections;

public class GroupChallengeCategoryFixture {

    /**
     * 기본 카테고리 (제로웨이스트) 생성
     */
    public static GroupChallengeCategory defaultCategory() {
        return of(GroupChallengeCategoryName.ZERO_WASTE);
    }

    /**
     * enum 기반으로 GroupChallengeCategory 생성
     */
    public static GroupChallengeCategory of(GroupChallengeCategoryName categoryName) {
        return GroupChallengeCategory.builder()
                // ID는 설정하지 않음 (Best Practice #2)
                .groupChallenges(Collections.emptyList())
                .name(categoryName.name())
                .imageUrl(GroupChallengeCategoryName.getImageUrl(categoryName.name()))
                .sequenceNumber(GroupChallengeCategoryName.getSequence(categoryName.name()))
                .activated(true)
                .build();
    }

    /**
     * name을 직접 문자열로 지정하고 싶은 경우 (유연성 확보용)
     */
    public static GroupChallengeCategory of(String name) {
        return GroupChallengeCategory.builder()
                // ID는 설정하지 않음
                .groupChallenges(Collections.emptyList())
                .name(name)
                .imageUrl("https://dummy.image/category/" + name.toLowerCase() + ".png")
                .sequenceNumber(1)
                .activated(true)
                .build();
    }
}
