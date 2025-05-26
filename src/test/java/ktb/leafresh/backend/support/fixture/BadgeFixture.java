package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.enums.BadgeType;

import java.util.ArrayList;

public class BadgeFixture {

    // 기본 EVENT 타입 사용
    public static Badge of(Long id, String name) {
        return of(id, name, BadgeType.EVENT);
    }

    // BadgeType을 명시적으로 지정하는 오버로드 메서드
    public static Badge of(Long id, String name, BadgeType type) {
        return Badge.builder()
                .id(id)
                .memberBadges(new ArrayList<>())
                .type(type)
                .name(name)
                .condition(getConditionByType(type, name))
                .imageUrl("https://dummy.image/badge/" + name + ".png")
                .build();
    }

    // 조건 문구는 테스트용으로 대략적인 구분만 해둡니다
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
