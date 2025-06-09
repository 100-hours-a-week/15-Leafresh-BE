package ktb.leafresh.backend.support.fixture;

import ktb.leafresh.backend.domain.feedback.domain.entity.Feedback;
import ktb.leafresh.backend.domain.member.domain.entity.Member;

import java.time.LocalDateTime;

public class FeedbackFixture {

    public static Feedback of(Member member) {
        return of(member, "테스트 피드백입니다.", LocalDateTime.now());
    }

    public static Feedback of(Member member, String content) {
        return of(member, content, LocalDateTime.now());
    }

    public static Feedback of(Member member, String content, LocalDateTime weekMonday) {
        return Feedback.of(member, content, weekMonday);
    }
}
