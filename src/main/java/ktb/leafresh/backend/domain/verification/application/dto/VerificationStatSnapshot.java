package ktb.leafresh.backend.domain.verification.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 인증글 통계 스냅샷 DTO
 * - 조회수 / 좋아요수 / 댓글수를 동기화 로그 출력용으로 활용
 */
@Getter
@AllArgsConstructor
public class VerificationStatSnapshot {

    private Long id;
    private int viewCount;
    private int likeCount;
    private int commentCount;
}
