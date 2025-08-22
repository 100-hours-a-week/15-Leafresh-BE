package ktb.leafresh.backend.domain.image.application.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import ktb.leafresh.backend.domain.image.presentation.dto.response.PresignedUrlResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Profile("eks")
@RequiredArgsConstructor
public class S3PresignedUrlProvider implements PresignedUrlProvider {

  private final AmazonS3 amazonS3;

  @Value("${aws.s3.bucket}")
  private String bucketName;

  private static final List<String> ALLOWED_CONTENT_TYPES =
      List.of("image/png", "image/jpeg", "image/jpg", "image/webp");

  @Override
  public PresignedUrlResponseDto generatePresignedUrl(String fileName, String contentType) {
    validateContentType(contentType);

    Date expiration = new Date(System.currentTimeMillis() + 3 * 60 * 1000);

    GeneratePresignedUrlRequest request =
        new GeneratePresignedUrlRequest(bucketName, fileName)
            .withMethod(HttpMethod.PUT)
            .withContentType(contentType)
            .withExpiration(expiration);

    URL uploadUrl = amazonS3.generatePresignedUrl(request);
    String fileUrl =
        String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s", bucketName, fileName);

    return new PresignedUrlResponseDto(uploadUrl.toString(), fileUrl);
  }

  private void validateContentType(String contentType) {
    if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
      throw new CustomException(GlobalErrorCode.UNSUPPORTED_CONTENT_TYPE);
    }
  }
}
