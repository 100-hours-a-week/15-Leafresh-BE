package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.enums.BadgeType;

public class BadgeFixture {

    public static final String DEFAULT_IMAGE_URL = "https://dummy.image/badge/";

    // BadgeType.EVENT 타입 기본 뱃지
    public static Badge of(String name) {
        return of(name, BadgeType.EVENT);
    }

    // BadgeType 지정 가능
    public static Badge of(String name, BadgeType type) {
        return Badge.builder()
                .type(type)
                .name(name)
                .condition(getConditionByType(type, name))
                .imageUrl(DEFAULT_IMAGE_URL + name + ".png")
                .build();
    }

    private static String getConditionByType(BadgeType type, String name) {
        return switch (type) {
            case GROUP -> name + " 카테고리 챌린지 10회 인증 시 지급";
            case PERSONAL -> name + " 연속 인증 달성 시 지급";
            case TOTAL -> name + " 누적 챌린지 인증 성공 수 기준 달성 시 지급";
            case SPECIAL -> "특정 조건 달성 시 지급되는 스페셜 뱃지";
            case EVENT -> "이벤트 인증 3회 성공 시 지급";
        };
    }
}
