package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.application.service.updater.GroupChallengeCategoryUpdater;
import ktb.leafresh.backend.domain.challenge.group.application.service.updater.GroupChallengeExampleImageUpdater;
import ktb.leafresh.backend.domain.challenge.group.application.service.updater.GroupChallengeUpdater;
import ktb.leafresh.backend.domain.challenge.group.application.validator.GroupChallengeDomainValidator;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.*;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeUpdateRequestDto;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupChallengeUpdateService {

    private final GroupChallengeUpdater challengeUpdater;
    private final GroupChallengeExampleImageUpdater imageUpdater;
    private final GroupChallengeCategoryUpdater categoryUpdater;
    private final GroupChallengeDomainValidator domainValidator;

    @Transactional
    public void update(Long memberId, Long challengeId, GroupChallengeUpdateRequestDto dto) {
        try {
            // 유효성 검사
            domainValidator.validate(dto);

            // 챌린지 정보 + 작성자 본인 확인
            GroupChallenge challenge = challengeUpdater.updateChallengeInfo(memberId, challengeId, dto);

            // 카테고리 수정
            categoryUpdater.updateCategory(challenge, dto.category());

            // 예시 이미지 처리 (권한 예외 발생 가능)
            imageUpdater.updateImages(challenge, dto.exampleImages());

        } catch (CustomException e) {
            // 위임
            throw e;
        } catch (SecurityException e) {
            // 예: 이미지 수정 권한 없음
            throw new CustomException(ChallengeErrorCode.CHALLENGE_UPDATE_IMAGE_PERMISSION_DENIED);
        } catch (Exception e) {
            // 예기치 못한 서버 오류
            throw new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_UPDATE_FAILED);
        }
    }
}
