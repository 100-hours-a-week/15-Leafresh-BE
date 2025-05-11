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
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class GcsService {

    private final Storage storage;

    @Value("${gcp.storage.bucket}")
    private String bucketName;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/png", "image/jpeg", "image/webp");

    public PresignedUrlResponseDto generateV4UploadPresignedUrl(String fileName, String contentType) {
        log.info("[PresignedUrl 요청] fileName={}, contentType={}", fileName, contentType);

        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            log.warn("지원하지 않는 Content-Type 요청됨: {}", contentType);
            throw new CustomException(GlobalErrorCode.UNSUPPORTED_CONTENT_TYPE);
        }

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
                .setContentType(contentType)
                .build();

        log.debug("BlobInfo 생성 완료 - bucket={}, fileName={}", bucketName, fileName);

        URL uploadUrl = storage.signUrl(blobInfo, 15, TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withV4Signature(),
                Storage.SignUrlOption.withContentType());

        String fileUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);

        log.info("Presigned URL 발급 완료 - uploadUrl={}, fileUrl={}", uploadUrl, fileUrl);

        return new PresignedUrlResponseDto(uploadUrl.toString(), fileUrl);
    }
}
