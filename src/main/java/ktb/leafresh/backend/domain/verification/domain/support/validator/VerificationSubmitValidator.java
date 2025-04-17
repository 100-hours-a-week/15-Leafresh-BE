package ktb.leafresh.backend.domain.verification.domain.support.validator;

import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.VerificationErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Component
public class VerificationSubmitValidator {

    public void validate(String content) {
        if (!StringUtils.hasText(content)) {
            throw new CustomException(VerificationErrorCode.CONTENT_REQUIRED);
        }
    }
}
