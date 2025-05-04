package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeParticipantRecordRepository;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupChallengeDeleteService {

    private final GroupChallengeRepository groupChallengeRepository;
    private final GroupChallengeParticipantRecordRepository participantRecordRepository;

    @Transactional
    public Long delete(Long memberId, Long challengeId) {
        log.info("단체 챌린지 삭제 요청 시작 - memberId: {}, challengeId: {}", memberId, challengeId);

        GroupChallenge challenge = groupChallengeRepository.findById(challengeId)
                .orElseThrow(() -> {
                    log.warn("단체 챌린지 삭제 실패 - 존재하지 않음: challengeId: {}", challengeId);
                    return new CustomException(ErrorCode.GROUP_CHALLENGE_NOT_FOUND);
                });

        if (challenge.getDeletedAt() != null) {
            log.warn("단체 챌린지 삭제 실패 - 이미 삭제됨: challengeId: {}", challengeId);
            throw new CustomException(ErrorCode.INVALID_REQUEST, "이미 삭제된 단체 챌린지입니다.");
        }

        if (!challenge.getMember().getId().equals(memberId)) {
            log.warn("단체 챌린지 삭제 실패 - 삭제 권한 없음: 요청자 ID={}, 작성자 ID={}", memberId, challenge.getMember().getId());
            throw new CustomException(ErrorCode.ACCESS_DENIED, "챌린지 삭제 권한이 없습니다.");
        }

        boolean hasParticipants = participantRecordRepository.existsByGroupChallengeIdAndDeletedAtIsNull(challengeId);
        if (hasParticipants) {
            log.warn("단체 챌린지 삭제 실패 - 참여자 존재: challengeId: {}", challengeId);
            throw new CustomException(ErrorCode.INVALID_REQUEST, "해당 챌린지에 참여자가 있어 삭제할 수 없습니다.");
        }

        // soft delete: 챌린지
        challenge.softDelete();
        log.info("단체 챌린지 soft delete 완료 - challengeId: {}", challengeId);

        // soft delete: 예시 이미지
        challenge.getExampleImages().forEach(image -> {
            image.softDelete();
            log.debug("예시 이미지 soft delete - imageId: {}, imageUrl: {}", image.getId(), image.getImageUrl());
        });

        log.info("단체 챌린지 삭제 전체 완료 - challengeId: {}", challengeId);
        return challengeId;
    }
}
