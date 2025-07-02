package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.member.domain.entity.Member;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;

public class GroupChallengeFixture {

    private static final String DEFAULT_TITLE = "제로웨이스트 챌린지";
    private static final String DEFAULT_DESCRIPTION = "지속가능한 삶을 위한 실천";
    private static final String DEFAULT_IMAGE_URL = "https://dummy.image/challenge.png";
    private static final int DEFAULT_LEAF_REWARD = 10;
    private static final int DEFAULT_MAX_PARTICIPANTS = 100;
    private static final int DEFAULT_CURRENT_PARTICIPANTS = 10;
    private static final LocalDateTime FIXED_START_DATE = LocalDateTime.of(2024, 1, 1, 0, 0);
    private static final LocalDateTime FIXED_END_DATE = LocalDateTime.of(2024, 1, 7, 23, 59);
    private static final LocalTime FIXED_VERIFICATION_START = LocalTime.of(6, 0);
    private static final LocalTime FIXED_VERIFICATION_END = LocalTime.of(22, 0);

    /**
     * 기본 그룹 챌린지 생성
     */
    public static GroupChallenge of(Member member, GroupChallengeCategory category) {
        return GroupChallenge.builder()
                .member(member)
                .category(category)
                .title(DEFAULT_TITLE)
                .description(DEFAULT_DESCRIPTION)
                .imageUrl(DEFAULT_IMAGE_URL)
                .leafReward(DEFAULT_LEAF_REWARD)
                .startDate(FIXED_START_DATE)
                .endDate(FIXED_END_DATE)
                .verificationStartTime(FIXED_VERIFICATION_START)
                .verificationEndTime(FIXED_VERIFICATION_END)
                .maxParticipantCount(DEFAULT_MAX_PARTICIPANTS)
                .currentParticipantCount(DEFAULT_CURRENT_PARTICIPANTS)
                .eventFlag(true)
                .exampleImages(Collections.emptyList())
                .build();
    }

    /**
     * 다양한 파라미터 테스트가 필요한 경우 오버로드 가능
     */
    public static GroupChallenge of(Member member, GroupChallengeCategory category, String title, boolean eventFlag) {
        return GroupChallenge.builder()
                .member(member)
                .category(category)
                .title(title)
                .description(DEFAULT_DESCRIPTION)
                .imageUrl(DEFAULT_IMAGE_URL)
                .leafReward(DEFAULT_LEAF_REWARD)
                .startDate(FIXED_START_DATE)
                .endDate(FIXED_END_DATE)
                .verificationStartTime(FIXED_VERIFICATION_START)
                .verificationEndTime(FIXED_VERIFICATION_END)
                .maxParticipantCount(DEFAULT_MAX_PARTICIPANTS)
                .currentParticipantCount(DEFAULT_CURRENT_PARTICIPANTS)
                .eventFlag(eventFlag)
                .exampleImages(Collections.emptyList())
                .build();
    }
}
