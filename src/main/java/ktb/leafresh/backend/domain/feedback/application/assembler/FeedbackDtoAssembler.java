package ktb.leafresh.backend.domain.feedback.application.assembler;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeParticipantRecordRepository;
import ktb.leafresh.backend.domain.feedback.infrastructure.dto.request.FeedbackCreationRequestDto;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.verification.domain.entity.PersonalChallengeVerification;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedbackDtoAssembler {

    private final PersonalChallengeVerificationRepository personalVerificationRepository;
    private final GroupChallengeParticipantRecordRepository groupRecordRepository;
    private final GroupChallengeVerificationRepository groupVerificationRepository;

    public FeedbackCreationRequestDto assemble(Long memberId, LocalDate start, LocalDate end) {
        List<FeedbackCreationRequestDto.PersonalChallengeDto> personalDtos =
                getPersonalChallengeDtos(memberId, start, end);
        List<FeedbackCreationRequestDto.GroupChallengeDto> groupDtos =
                getGroupChallengeDtos(memberId, start, end);

        return FeedbackCreationRequestDto.builder()
                .memberId(memberId)
                .personalChallenges(personalDtos)
                .groupChallenges(groupDtos)
                .build();
    }

    private List<FeedbackCreationRequestDto.PersonalChallengeDto> getPersonalChallengeDtos(Long memberId, LocalDate start, LocalDate end) {
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.atTime(23, 59, 59);

        List<PersonalChallengeVerification> verifications =
                personalVerificationRepository.findWeeklyVerifications(memberId, startDt, endDt);

        log.debug("[개인 인증 수집 완료] count={}", verifications.size());

        return verifications.stream()
                .collect(Collectors.groupingBy(v -> v.getPersonalChallenge().getId()))
                .entrySet().stream()
                .map(entry -> {
                    PersonalChallengeVerification sample = entry.getValue().get(0);
                    boolean isSuccess = entry.getValue().stream()
                            .anyMatch(v -> v.getStatus() == ChallengeStatus.SUCCESS);
                    return FeedbackCreationRequestDto.PersonalChallengeDto.builder()
                            .id(sample.getPersonalChallenge().getId())
                            .title(sample.getPersonalChallenge().getTitle())
                            .isSuccess(isSuccess)
                            .build();
                }).toList();
    }

    private List<FeedbackCreationRequestDto.GroupChallengeDto> getGroupChallengeDtos(Long memberId, LocalDate start, LocalDate end) {
        List<GroupChallengeParticipantRecord> records = groupRecordRepository.findAllByMemberId(memberId);
        log.debug("[단체 참여 기록 조회] count={}", records.size());

        List<Long> recordIds = records.stream()
                .map(GroupChallengeParticipantRecord::getId)
                .toList();

        List<GroupChallengeVerification> allVerifications =
                groupVerificationRepository.findAllByParticipantRecordIds(recordIds);

        Map<Long, List<GroupChallengeVerification>> groupedVerifications = allVerifications.stream()
                .filter(v -> v.getVerifiedAt() != null)
                .collect(Collectors.groupingBy(v -> v.getParticipantRecord().getId()));

        List<FeedbackCreationRequestDto.GroupChallengeDto> result = new ArrayList<>();

        for (GroupChallengeParticipantRecord record : records) {
            List<GroupChallengeVerification> verifications = groupedVerifications.getOrDefault(record.getId(), List.of());

            List<FeedbackCreationRequestDto.GroupChallengeDto.SubmissionDto> submissions = verifications.stream()
                    .filter(v -> {
                        LocalDate d = v.getVerifiedAt().toLocalDate();
                        return !d.isBefore(start) && !d.isAfter(end);
                    })
                    .map(this::toSubmissionDto)
                    .collect(Collectors.toList());

            // 인증이 없어도 챌린지 자체는 포함됨
            result.add(FeedbackCreationRequestDto.GroupChallengeDto.builder()
                    .id(record.getGroupChallenge().getId())
                    .title(record.getGroupChallenge().getTitle())
                    .startDate(record.getGroupChallenge().getStartDate())
                    .endDate(record.getGroupChallenge().getEndDate())
                    .submissions(submissions)
                    .build());
        }

        return result;
    }

    private FeedbackCreationRequestDto.GroupChallengeDto.SubmissionDto toSubmissionDto(GroupChallengeVerification v) {
        return FeedbackCreationRequestDto.GroupChallengeDto.SubmissionDto.builder()
                .isSuccess(v.getStatus() == ChallengeStatus.SUCCESS)
                .submittedAt(v.getVerifiedAt())
                .build();
    }
}
