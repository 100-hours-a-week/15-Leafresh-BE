package ktb.leafresh.backend.domain.member.domain.entity.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LoginType {
    KAKAO, NAVER, GOOGLE;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static LoginType from(String value) {
        for (LoginType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new RuntimeException("지원하지 않는 provider입니다: " + value); // 무조건 예외 잡힘
    }

    @JsonValue
    public String toJson() {
        return name();
    }
}
