package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.feedback.domain.entity.Feedback;
import ktb.leafresh.backend.domain.member.domain.entity.Member;

import java.time.LocalDateTime;

public class FeedbackFixture {

    private static final String DEFAULT_CONTENT = "테스트 피드백입니다.";
    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2024, 1, 1, 12, 0);

    /**
     * 기본값으로 Feedback 생성 (content 고정, 시간 고정)
     */
    public static Feedback of(Member member) {
        return of(member, DEFAULT_CONTENT, FIXED_NOW);
    }

    /**
     * content만 지정하는 생성자 (시간은 고정)
     */
    public static Feedback of(Member member, String content) {
        return of(member, content, FIXED_NOW);
    }

    /**
     * content와 시간 모두 지정
     */
    public static Feedback of(Member member, String content, LocalDateTime createdAt) {
        return Feedback.of(member, content, createdAt);
    }
}
