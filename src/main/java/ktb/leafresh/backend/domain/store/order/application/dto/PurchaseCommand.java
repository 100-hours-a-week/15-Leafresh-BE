package ktb.leafresh.backend.domain.store.order.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "구매 명령 객체")
public record PurchaseCommand(
    @Schema(description = "회원 ID", example = "1") Long memberId,
    @Schema(description = "상품 ID", example = "1") Long productId,
    @Schema(description = "타임딜 정책 ID (타임딜 상품 구매 시에만 사용)", example = "1") Long timedealPolicyId,
    @Schema(description = "구매 수량", example = "2") Integer quantity,
    @Schema(description = "멱등성 키", example = "purchase_123456_uuid") String idempotencyKey,
    @Schema(description = "요청 시간", example = "2024-12-01T10:30:00") LocalDateTime requestedAt) {}
