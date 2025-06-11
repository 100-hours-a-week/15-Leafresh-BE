package ktb.leafresh.backend.domain.challenge.group.domain.entity.enums;

public enum GroupChallengeCategoryName {

    ALL("전체"),
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

    public static String getImageUrl(String name) {
        return switch (name) {
            case "ALL" -> "https://storage.googleapis.com/leafresh-images/init/all.png";
            case "ZERO_WASTE" -> "https://storage.googleapis.com/leafresh-images/init/zero_waste.png";
            case "PLOGGING" -> "https://storage.googleapis.com/leafresh-images/init/plogging.png";
            case "CARBON_FOOTPRINT" -> "https://storage.googleapis.com/leafresh-images/init/carbon_footprint.png";
            case "ENERGY_SAVING" -> "https://storage.googleapis.com/leafresh-images/init/energy_saving.png";
            case "UPCYCLING" -> "https://storage.googleapis.com/leafresh-images/init/upcycling.png";
            case "MEDIA" -> "https://storage.googleapis.com/leafresh-images/init/media.png";
            case "DIGITAL_CARBON" -> "https://storage.googleapis.com/leafresh-images/init/digital_carbon.png";
            case "VEGAN" -> "https://storage.googleapis.com/leafresh-images/init/vegan.png";
            case "ETC" -> "https://storage.googleapis.com/leafresh-images/init/etc.png";
            default -> "default_image_url";
        };
    }

    public static int getSequence(String name) {
        return GroupChallengeCategoryName.valueOf(name).ordinal() + 1;
    }

    public static GroupChallengeCategoryName[] seeds() {
        return values();
    }
}
