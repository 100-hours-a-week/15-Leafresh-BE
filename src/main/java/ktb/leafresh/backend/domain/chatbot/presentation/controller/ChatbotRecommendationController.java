package ktb.leafresh.backend.domain.chatbot.presentation.controller;

import jakarta.validation.Valid;
import ktb.leafresh.backend.domain.chatbot.application.service.ChatbotRecommendationService;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotBaseInfoRequestDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.request.ChatbotFreeTextRequestDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.response.ChatbotBaseInfoResponseDto;
import ktb.leafresh.backend.domain.chatbot.presentation.dto.response.ChatbotFreeTextResponseDto;
import ktb.leafresh.backend.global.response.ApiResponse;
import ktb.leafresh.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/chatbot/recommendation")
@RequiredArgsConstructor
public class ChatbotRecommendationController {

    private final ChatbotRecommendationService recommendationService;

    @PostMapping("/base-info")
    public ResponseEntity<ApiResponse<ChatbotBaseInfoResponseDto>> recommendByBaseInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ChatbotBaseInfoRequestDto requestDto
    ) {
        log.info("[챗봇 추천 요청 - 기본 정보] location={}, workType={}, category={}",
                requestDto.location(), requestDto.workType(), requestDto.category());

        if (userDetails == null) {
            log.warn("[챗봇 추천 실패] 인증 정보가 없습니다.");
            throw new IllegalStateException("로그인 정보가 존재하지 않습니다.");
        }

        Long memberId = userDetails.getMemberId();
        log.info("[챗봇 추천 요청 - 기본 정보] memberId={}", memberId);

        ChatbotBaseInfoResponseDto response = recommendationService.recommendByBaseInfo(memberId, requestDto);
        log.info("[챗봇 추천 완료 - 기본 정보] 추천 결과: {}", response.recommend());

        return ResponseEntity.ok(ApiResponse.success("사용자 기본 정보 키워드 선택을 기반으로 챌린지를 추천합니다.", response));
    }

    @PostMapping("/free-text")
    public ResponseEntity<ApiResponse<ChatbotFreeTextResponseDto>> recommendByFreeText(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ChatbotFreeTextRequestDto requestDto
    ) {
        log.info("[챗봇 추천 요청 - 자유 텍스트] location={}, workType={}, message={}",
                requestDto.location(), requestDto.workType(), requestDto.message());

        if (userDetails == null) {
            log.warn("[챗봇 추천 실패] 인증 정보가 없습니다.");
            throw new IllegalStateException("로그인 정보가 존재하지 않습니다.");
        }

        Long memberId = userDetails.getMemberId();
        log.info("[챗봇 추천 요청 - 자유 텍스트] memberId={}", memberId);

        ChatbotFreeTextResponseDto response = recommendationService.recommendByFreeText(memberId, requestDto);
        log.info("[챗봇 추천 완료 - 자유 텍스트] 추천 결과: {}", response.recommend());

        return ResponseEntity.ok(ApiResponse.success("사용자 자유 메시지를 기반으로 챌린지를 추천합니다.", response));
    }
}
