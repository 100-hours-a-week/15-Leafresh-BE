package ktb.leafresh.backend.domain.image.presentation.controller;

import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.image.application.service.GcsService;
import ktb.leafresh.backend.domain.image.presentation.dto.request.PresignedUrlRequestDto;
import ktb.leafresh.backend.domain.image.presentation.dto.response.PresignedUrlResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/s3/images")
@Profile("!eks")
public class GcsController {

    private final GcsService gcsService;

    @PostMapping("/presigned-url")
    public ResponseEntity<ApiResponse<PresignedUrlResponseDto>> getPresignedUrl(
            @Valid @RequestBody PresignedUrlRequestDto requestDto) {

        log.info("[PresignedUrl 요청 수신] fileName={}, contentType={}", requestDto.fileName(), requestDto.contentType());

        // 유효성 검증 통과됨
        log.debug("요청 본문 유효성 검증 완료");

        log.debug("GcsService.generateV4UploadPresignedUrl 호출 시작");
        PresignedUrlResponseDto response = gcsService.generateV4UploadPresignedUrl(
                requestDto.fileName(),
                requestDto.contentType()
        );

        log.debug("GcsService.generateV4UploadPresignedUrl 호출 완료");

        log.info("PresignedUrl 발급 성공 - fileUrl={}", response.fileUrl());

        return ResponseEntity.ok(ApiResponse.success("presigned url을 발급받았습니다.", response));
    }
}
