package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.application.service.updater.GroupChallengeCategoryUpdater;
import ktb.leafresh.backend.domain.challenge.group.application.service.updater.GroupChallengeExampleImageUpdater;
import ktb.leafresh.backend.domain.challenge.group.application.service.updater.GroupChallengeUpdater;
import ktb.leafresh.backend.domain.challenge.group.application.validator.GroupChallengeDomainValidator;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.*;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeUpdateRequestDto;
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
        // 유효성 검사
        domainValidator.validate(dto);

        // 챌린지 정보 + 작성자 본인 확인
        GroupChallenge challenge = challengeUpdater.updateChallengeInfo(memberId, challengeId, dto);

        // 카테고리 수정
        categoryUpdater.updateCategory(challenge, dto.category());

        // 예시 이미지 처리
        imageUpdater.updateImages(challenge, dto.exampleImages());
    }
}
