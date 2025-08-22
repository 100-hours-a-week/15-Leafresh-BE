package ktb.leafresh.backend.domain.challenge.group.application.service.updater;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeExampleImage;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeExampleImageRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeUpdateRequestDto;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.util.image.ImageEntityUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupChallengeExampleImageUpdater {

  private final GroupChallengeExampleImageRepository repository;
  private final ImageEntityUpdater imageEntityUpdater;

  public void updateImages(
      GroupChallenge challenge, GroupChallengeUpdateRequestDto.ExampleImages exampleImages) {
    Long ownerId = challenge.getMember().getId();

    // 1. keep 목록에 대한 권한 체크
    for (var keep : exampleImages.keep()) {
      GroupChallengeExampleImage image =
          repository
              .findById(keep.id())
              .orElseThrow(() -> new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_NOT_FOUND));
      if (!image.getGroupChallenge().getMember().getId().equals(ownerId)) {
        throw new CustomException(ChallengeErrorCode.CHALLENGE_UPDATE_IMAGE_PERMISSION_DENIED);
      }
    }

    // 2. deleted 목록에 대한 권한 체크
    for (Long deletedId : exampleImages.deleted()) {
      GroupChallengeExampleImage image =
          repository
              .findById(deletedId)
              .orElseThrow(() -> new CustomException(ChallengeErrorCode.GROUP_CHALLENGE_NOT_FOUND));
      if (!image.getGroupChallenge().getMember().getId().equals(ownerId)) {
        throw new CustomException(ChallengeErrorCode.CHALLENGE_UPDATE_IMAGE_PERMISSION_DENIED);
      }
    }

    // 3. 기존 로직 수행
    List<ImageEntityUpdater.KeepImage> keepList =
        exampleImages.keep().stream()
            .map(k -> new ImageEntityUpdater.KeepImage(k.id(), k.sequenceNumber()))
            .toList();

    List<GroupChallengeExampleImage> newEntities =
        exampleImages.newImages().stream()
            .map(
                n ->
                    GroupChallengeExampleImage.of(
                        challenge, n.imageUrl(), n.type(), n.description(), n.sequenceNumber()))
            .toList();

    imageEntityUpdater.update(
        challenge, keepList, newEntities, exampleImages.deleted(), repository);
  }
}
