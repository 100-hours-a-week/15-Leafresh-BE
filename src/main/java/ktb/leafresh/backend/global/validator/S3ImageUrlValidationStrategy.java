package ktb.leafresh.backend.global.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("eks")
public class S3ImageUrlValidationStrategy implements ImageUrlValidationStrategy {

  @Value("${aws.s3.bucket}")
  private String bucketName;

  @Override
  public boolean isValid(String value) {
    // null 허용: 아예 안 보낸 건 업데이트 안 하겠다는 뜻이므로 유효
    if (value == null) return true;

    // 값이 있다면 유효한 S3 URL인지 확인
    return value.startsWith("https://" + bucketName + ".s3.ap-northeast-2.amazonaws.com/");
  }
}
