package ktb.leafresh.backend.global.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

public class GcsImageUrlValidator implements ConstraintValidator<ValidGcsImageUrl, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return StringUtils.hasText(value)
                && value.startsWith("https://storage.googleapis.com/");
    }
}
