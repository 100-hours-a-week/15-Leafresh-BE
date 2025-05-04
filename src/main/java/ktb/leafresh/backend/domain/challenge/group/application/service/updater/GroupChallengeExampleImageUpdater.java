package ktb.leafresh.backend.domain.challenge.group.application.service.updater;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeExampleImage;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeExampleImageRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeUpdateRequestDto;
import ktb.leafresh.backend.global.util.image.ImageEntityUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupChallengeExampleImageUpdater {

    private final GroupChallengeExampleImageRepository repository;
    private final ImageEntityUpdater imageEntityUpdater;

    public void updateImages(GroupChallenge challenge, GroupChallengeUpdateRequestDto.ExampleImages exampleImages) {
        List<ImageEntityUpdater.KeepImage> keepList = exampleImages.keep()
                .stream()
                .map(k -> new ImageEntityUpdater.KeepImage(k.id(), k.sequenceNumber()))
                .toList();

        List<GroupChallengeExampleImage> newEntities = exampleImages.newImages()
                .stream()
                .map(n -> GroupChallengeExampleImage.of(
                        challenge, n.imageUrl(), n.type(), n.description(), n.sequenceNumber()
                ))
                .toList();

        imageEntityUpdater.update(challenge, keepList, newEntities, exampleImages.deleted(), repository);
    }
}
