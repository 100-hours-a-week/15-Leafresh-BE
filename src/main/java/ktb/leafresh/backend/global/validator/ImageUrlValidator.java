package ktb.leafresh.backend.global.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ImageUrlValidator implements ConstraintValidator<ValidImageUrl, String> {

    private final List<ImageUrlValidationStrategy> strategies;

    private ImageUrlValidationStrategy selected;

    @Override
    public void initialize(ValidImageUrl constraintAnnotation) {
        // 현재 환경에 활성화된 전략이 1개뿐이어야 함
        if (strategies.size() != 1) {
            throw new IllegalStateException("Exactly one ImageUrlValidationStrategy must be active");
        }
        selected = strategies.get(0);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return selected.isValid(value);
    }
}
