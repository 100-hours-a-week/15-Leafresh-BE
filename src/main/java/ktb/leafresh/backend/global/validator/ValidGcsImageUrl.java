package ktb.leafresh.backend.global.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = GcsImageUrlValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidGcsImageUrl {

    String message() default "유효하지 않은 GCS 이미지 URL입니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
