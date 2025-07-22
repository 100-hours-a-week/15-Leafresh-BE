package ktb.leafresh.backend.domain.image.application.service;

import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import ktb.leafresh.backend.domain.image.presentation.dto.response.PresignedUrlResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Profile("!eks")
@RequiredArgsConstructor
public class GcsPresignedUrlProvider implements PresignedUrlProvider {

    private final Storage storage;

    @Value("${gcp.storage.bucket}")
    private String bucketName;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/png", "image/jpeg", "image/jpg", "image/webp");

    @Override
    public PresignedUrlResponseDto generatePresignedUrl(String fileName, String contentType) {
        validateContentType(contentType);

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
                .setContentType(contentType)
                .build();

        URL uploadUrl = storage.signUrl(blobInfo, 3, TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withV4Signature(),
                Storage.SignUrlOption.withContentType());

        String fileUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);

        return new PresignedUrlResponseDto(uploadUrl.toString(), fileUrl);
    }

    private void validateContentType(String contentType) {
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new CustomException(GlobalErrorCode.UNSUPPORTED_CONTENT_TYPE);
        }
    }
}
