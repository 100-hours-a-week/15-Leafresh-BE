package ktb.leafresh.backend.global.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

public class GcsImageUrlValidator implements ConstraintValidator<ValidGcsImageUrl, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null 허용: 아예 안 보낸 건 업데이트 안 하겠다는 뜻이므로 유효
        if (value == null) return true;

        // 값이 있다면 유효한 GCS URL인지 확인
        return StringUtils.hasText(value)
                && value.startsWith("https://storage.googleapis.com/");
    }
}
