package ktb.leafresh.backend.global.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ImageUrlValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidImageUrl {

    String message() default "유효하지 않은 이미지 URL입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
