package ktb.leafresh.backend.domain.challenge.group.domain.entity.enums;

import java.util.Arrays;

public enum GroupChallengeCategoryName {

    ZERO_WASTE("제로웨이스트"),
    PLOGGING("플로깅"),
    CARBON_FOOTPRINT("탄소 발자국"),
    ENERGY_SAVING("에너지 절약"),
    UPCYCLING("업사이클"),
    MEDIA("문화 공유"),
    DIGITAL_CARBON("디지털 탄소"),
    VEGAN("비건"),
    ETC("기타");

    private final String label; // 한글 라벨

    GroupChallengeCategoryName(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static String toEnglish(String koreanInput) {
        return Arrays.stream(values())
                .filter(v -> v.label.equals(koreanInput))
                .map(Enum::name)
                .findFirst()
                .orElse(null);
    }

    public static String getImageUrl(String name) {
        return switch (name) {
            case "ZERO_WASTE" -> "imageUrl1";
            case "PLOGGING" -> "imageUrl2";
            case "CARBON_FOOTPRINT" -> "imageUrl3";
            case "ENERGY_SAVING" -> "imageUrl4";
            case "UPCYCLING" -> "imageUrl5";
            case "MEDIA" -> "imageUrl6";
            case "DIGITAL_CARBON" -> "imageUrl7";
            case "VEGAN" -> "imageUrl8";
            case "ETC" -> "imageUrl9";
            default -> "defaultImageUrl";
        };
    }

    public static int getSequence(String name) {
        return GroupChallengeCategoryName.valueOf(name).ordinal() + 1;
    }

    public static GroupChallengeCategoryName[] seeds() {
        return values();
    }
}
