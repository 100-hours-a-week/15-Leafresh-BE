//package ktb.leafresh.backend.domain.image.presentation.controller;
//
//import jakarta.validation.Valid;
//import ktb.leafresh.backend.domain.image.application.service.S3Service;
//import ktb.leafresh.backend.domain.image.presentation.dto.request.PresignedUrlRequestDto;
//import ktb.leafresh.backend.domain.image.presentation.dto.response.PresignedUrlResponseDto;
//import ktb.leafresh.backend.global.response.ApiResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Profile;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/s3/images")
//@Profile("eks")
//public class S3Controller {
//
//    private final S3Service s3Service;
//
//    @PostMapping("/presigned-url")
//    public ResponseEntity<ApiResponse<PresignedUrlResponseDto>> getPresignedUrl(
//            @Valid @RequestBody PresignedUrlRequestDto requestDto) {
//
//        log.info("[S3 PresignedUrl 요청 수신] fileName={}, contentType={}", requestDto.fileName(), requestDto.contentType());
//
//        PresignedUrlResponseDto response = s3Service.generatePresignedUrl(
//                requestDto.fileName(),
//                requestDto.contentType()
//        );
//
//        log.info("PresignedUrl 발급 성공 - fileUrl={}", response.fileUrl());
//
//        return ResponseEntity.ok(ApiResponse.success("presigned url을 발급받았습니다.", response));
//    }
//}
