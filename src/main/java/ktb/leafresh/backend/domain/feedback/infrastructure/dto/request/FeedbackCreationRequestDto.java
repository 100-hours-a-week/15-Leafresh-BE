package ktb.leafresh.backend.domain.feedback.infrastructure.dto.request;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record FeedbackCreationRequestDto(
        Long memberId,
        List<PersonalChallengeDto> personalChallenges,
        List<GroupChallengeDto> groupChallenges
) {

    @Builder
    public record PersonalChallengeDto(
            Long id,
            String title,
            boolean isSuccess
    ) {}

    @Builder
    public record GroupChallengeDto(
            Long id,
            String title,
            LocalDateTime startDate,
            LocalDateTime endDate,
            List<SubmissionDto> submissions
    ) {
        public GroupChallengeDto {
            submissions = submissions != null ? submissions : List.of();
        }

        @Builder
        public record SubmissionDto(
                boolean isSuccess,
                LocalDateTime submittedAt
        ) {}
    }
}
