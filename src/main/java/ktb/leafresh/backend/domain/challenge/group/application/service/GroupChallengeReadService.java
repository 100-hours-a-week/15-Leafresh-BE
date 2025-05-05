package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeQueryRepository;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeVerificationQueryRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.response.*;
import ktb.leafresh.backend.domain.verification.domain.entity.GroupChallengeVerification;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ErrorCode;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationHelper;
import ktb.leafresh.backend.global.util.pagination.CursorPaginationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupChallengeReadService {

    private final GroupChallengeQueryRepository groupChallengeQueryRepository;
    private final GroupChallengeRepository groupChallengeRepository;
    private final GroupChallengeVerificationRepository verificationRepository;
    private final GroupChallengeVerificationQueryRepository groupChallengeVerificationQueryRepository;

    public GroupChallengeListResponseDto getGroupChallenges(String input, String category, Long cursorId, int size) {
        if (category == null || category.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        CursorPaginationResult<GroupChallengeSummaryDto> page = CursorPaginationHelper.paginate(
                groupChallengeQueryRepository.findByFilter(input, category, cursorId, size + 1),
                size,
                GroupChallengeSummaryDto::from,
                GroupChallengeSummaryDto::id
        );

        return GroupChallengeListResponseDto.builder()
                .groupChallenges(page.items())
                .hasNext(page.hasNext())
                .lastCursorId(page.lastCursorId())
                .build();
    }

    public GroupChallengeDetailResponseDto getChallengeDetail(Long memberIdOrNull, Long challengeId) {
        GroupChallenge challenge = getChallengeOrThrow(challengeId);
        List<String> verificationImages = getVerificationImages(challengeId);
        List<GroupChallengeExampleImageDto> exampleImages = getExampleImages(challenge);
        ChallengeStatus status = resolveChallengeStatus(memberIdOrNull, challengeId);

        return GroupChallengeDetailResponseDto.of(challenge, exampleImages, verificationImages, status);
    }

    private GroupChallenge getChallengeOrThrow(Long challengeId) {
        return groupChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_CHALLENGE_NOT_FOUND));
    }

    private List<String> getVerificationImages(Long challengeId) {
        return verificationRepository
                .findTop9ByParticipantRecord_GroupChallenge_IdOrderByCreatedAtDesc(challengeId)
                .stream()
                .map(GroupChallengeVerification::getImageUrl)
                .toList();
    }

    private List<GroupChallengeExampleImageDto> getExampleImages(GroupChallenge challenge) {
        return challenge.getExampleImages().stream()
                .map(GroupChallengeExampleImageDto::from)
                .toList();
    }

    private ChallengeStatus resolveChallengeStatus(Long memberIdOrNull, Long challengeId) {
        if (memberIdOrNull == null) {
            log.info("비로그인 상태 - 인증 상태 조회 생략");
            return ChallengeStatus.NOT_SUBMITTED;
        }

        log.info("로그인 상태 - memberId = {}", memberIdOrNull);
        return verificationRepository
                .findTopByParticipantRecord_Member_IdAndParticipantRecord_GroupChallenge_IdOrderByCreatedAtDesc(memberIdOrNull, challengeId)
                .map(GroupChallengeVerification::getStatus)
                .orElse(ChallengeStatus.NOT_SUBMITTED);
    }

    public GroupChallengeVerificationListResponseDto getVerifications(
            Long challengeId, Long cursorId, int size
    ) {
        CursorPaginationResult<GroupChallengeVerificationSummaryDto> page = CursorPaginationHelper.paginate(
                groupChallengeVerificationQueryRepository.findByChallengeId(challengeId, cursorId, size + 1),
                size,
                GroupChallengeVerificationSummaryDto::from,
                GroupChallengeVerificationSummaryDto::id
        );

        return GroupChallengeVerificationListResponseDto.builder()
                .verifications(page.items())
                .hasNext(page.hasNext())
                .lastCursorId(page.lastCursorId())
                .build();
    }
}
