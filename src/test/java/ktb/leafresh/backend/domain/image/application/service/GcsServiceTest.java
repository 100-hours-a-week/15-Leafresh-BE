// package ktb.leafresh.backend.domain.image.application.service;
//
// import com.google.cloud.storage.BlobInfo;
// import com.google.cloud.storage.HttpMethod;
// import com.google.cloud.storage.Storage;
// import ktb.leafresh.backend.domain.image.presentation.dto.response.PresignedUrlResponseDto;
// import ktb.leafresh.backend.global.exception.CustomException;
// import ktb.leafresh.backend.global.exception.GlobalErrorCode;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.test.util.ReflectionTestUtils;
//
// import java.net.URL;
// import java.util.concurrent.TimeUnit;
//
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.catchThrowableOfType;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;
//
// class GcsServiceTest {
//
//    private Storage storage;
//    private GcsService gcsService;
//
//    @BeforeEach
//    void setUp() {
//        storage = mock(Storage.class);
//        gcsService = new GcsService(storage);
//        ReflectionTestUtils.setField(gcsService, "bucketName", "test-bucket");
//    }
//
//    @Test
//    @DisplayName("유효한 이미지 타입에 대해 Presigned URL을 반환한다")
//    void generatePresignedUrl_success() throws Exception {
//        // given
//        String fileName = "image.png";
//        String contentType = "image/png";
//        URL dummyUrl = new URL("https://signed.upload.url");
//
//        BlobInfo expectedBlobInfo = BlobInfo.newBuilder("test-bucket", fileName)
//                .setContentType(contentType)
//                .build();
//
//        when(storage.signUrl(
//                any(BlobInfo.class),
//                eq(3L),
//                eq(TimeUnit.MINUTES),
//                any(Storage.SignUrlOption[].class)
//        )).thenReturn(dummyUrl);
//
//        // when
//        PresignedUrlResponseDto result = gcsService.generateV4UploadPresignedUrl(fileName,
// contentType);
//
//        // then
//        assertThat(result.uploadUrl()).isEqualTo("https://signed.upload.url");
//
// assertThat(result.fileUrl()).isEqualTo("https://storage.googleapis.com/test-bucket/image.png");
//    }
//
//    @Test
//    @DisplayName("지원하지 않는 Content-Type이면 예외를 던진다")
//    void generatePresignedUrl_fail_unsupportedContentType() {
//        // given
//        String fileName = "hacked.exe";
//        String contentType = "application/octet-stream";
//
//        // when
//        CustomException ex = catchThrowableOfType(
//                () -> gcsService.generateV4UploadPresignedUrl(fileName, contentType),
//                CustomException.class
//        );
//
//        // then
//        assertThat(ex).isNotNull();
//        assertThat(ex.getErrorCode()).isEqualTo(GlobalErrorCode.UNSUPPORTED_CONTENT_TYPE);
//    }
// }
