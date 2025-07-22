//package ktb.leafresh.backend.domain.image.application.service;
//
//import com.amazonaws.HttpMethod;
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
//import ktb.leafresh.backend.domain.image.presentation.dto.response.PresignedUrlResponseDto;
//import ktb.leafresh.backend.global.exception.CustomException;
//import ktb.leafresh.backend.global.exception.GlobalErrorCode;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Service;
//
//import java.net.URL;
//import java.util.Date;
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@Profile("eks")
//public class S3Service {
//
//    private final AmazonS3 amazonS3;
//
//    @Value("${aws.s3.bucket}")
//    private String bucketName;
//
//    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/png", "image/jpeg", "image/jpg", "image/webp");
//
//    public PresignedUrlResponseDto generatePresignedUrl(String fileName, String contentType) {
//        log.info("[S3 Presigned URL 요청] fileName={}, contentType={}", fileName, contentType);
//
//        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
//            log.warn("지원하지 않는 Content-Type 요청됨: {}", contentType);
//            throw new CustomException(GlobalErrorCode.UNSUPPORTED_CONTENT_TYPE);
//        }
//
//        // 만료 시간 3분
//        Date expiration = new Date(System.currentTimeMillis() + 3 * 60 * 1000);
//
//        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, fileName)
//                .withMethod(HttpMethod.PUT)
//                .withContentType(contentType)
//                .withExpiration(expiration);
//
//        URL uploadUrl = amazonS3.generatePresignedUrl(request);
//        String fileUrl = String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s", bucketName, fileName);
//
//        log.info("Presigned URL 발급 완료 - uploadUrl={}, fileUrl={}", uploadUrl, fileUrl);
//
//        return new PresignedUrlResponseDto(uploadUrl.toString(), fileUrl);
//    }
//}
