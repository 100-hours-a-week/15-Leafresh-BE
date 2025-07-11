package ktb.leafresh.backend.global.validator;

import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;

public class NicknameValidator {

    private static final String REGEX = "^[a-zA-Z0-9가-힣]{1,20}$";

    public static void validate(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new CustomException(MemberErrorCode.NICKNAME_REQUIRED);
        }

        if (!nickname.matches(REGEX)) {
            throw new CustomException(MemberErrorCode.NICKNAME_INVALID_FORMAT);
        }
    }
}
