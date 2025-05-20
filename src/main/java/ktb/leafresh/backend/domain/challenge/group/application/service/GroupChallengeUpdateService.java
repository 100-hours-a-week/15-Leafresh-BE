package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.application.service.updater.GroupChallengeCategoryUpdater;
import ktb.leafresh.backend.domain.challenge.group.application.service.updater.GroupChallengeExampleImageUpdater;
import ktb.leafresh.backend.domain.challenge.group.application.service.updater.GroupChallengeUpdater;
import ktb.leafresh.backend.domain.challenge.group.application.validator.GroupChallengeDomainValidator;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeParticipantRecordRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeUpdateRequestDto;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupChallengeUpdateService {

    private final GroupChallengeUpdater challengeUpdater;
    private final GroupChallengeExampleImageUpdater imageUpdater;
    private final GroupChallengeCategoryUpdater categoryUpdater;
    private final GroupChallengeDomainValidator domainValidator;
    private final GroupChallengeParticipantRecordRepository participantRecordRepository;

    @Transactional
    public void update(Long memberId, Long challengeId, GroupChallengeUpdateRequestDto dto) {
        log.info("단체 챌린지 수정 요청 시작 - memberId: {}, challengeId: {}", memberId, challengeId);

        try {
            // 1단계: 챌린지 참여자 존재 여부 먼저 확인
            boolean hasParticipants = participantRecordRepository.existsByGroupChallengeIdAndDeletedAtIsNull(challengeId);
            if (hasParticipants) {
                log.warn("단체 챌린지 수정 실패 - 참여자 존재: challengeId: {}", challengeId);
                throw new CustomException(ChallengeErrorCode.CHALLENGE_HAS_PARTICIPANTS_UPDATE_NOT_ALLOWED);
            }
            log.debug("참여자 없음 확인 - 수정 계속 진행: challengeId: {}", challengeId);

            // 2단계: 유효성 검사
            domainValidator.validate(dto);
            log.debug("단체 챌린지 유효성 검사 통과 - challengeId: {}", challengeId);

            // 3단계: 챌린지 정보 업데이트 + 권한 확인
            GroupChallenge challenge = challengeUpdater.updateChallengeInfo(memberId, challengeId, dto);
            log.debug("챌린지 기본 정보 업데이트 완료 - challengeId: {}", challengeId);

            // 4단계: 카테고리 변경
            categoryUpdater.updateCategory(challenge, dto.category());
            log.debug("챌린지 카테고리 업데이트 완료 - challengeId: {}", challengeId);

            // 5단계: 이미지 수정
            imageUpdater.updateImages(challenge, dto.exampleImages());
            log.debug("챌린지 이미지 업데이트 완료 - challengeId: {}", challengeId);

            log.info("단체 챌린지 수정 전체 완료 - challengeId: {}", challengeId);

        } catch (CustomException e) {
            log.warn("단체 챌린지 수정 중 CustomException 발생 - message: {}", e.getMessage());
            throw e;
        } catch (SecurityException e) {
            log.warn("단체 챌린지 이미지 권한 예외 발생 - challengeId: {}", challengeId);
            throw new CustomException(ChallengeErrorCode.CHALLENGE_UPDATE_IMAGE_PERMISSION_DENIED);
        } catch (Exception e) {
            log.error("단체 챌린지 수정 실패 - 예기치 못한 서버 오류: {}", e.getMessage(), e);
            throw new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_UPDATE_FAILED);
        }
    }
}
