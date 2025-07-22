package ktb.leafresh.backend.domain.image.application.service;

import ktb.leafresh.backend.domain.image.presentation.dto.response.PresignedUrlResponseDto;

public interface PresignedUrlProvider {
    PresignedUrlResponseDto generatePresignedUrl(String fileName, String contentType);
}
