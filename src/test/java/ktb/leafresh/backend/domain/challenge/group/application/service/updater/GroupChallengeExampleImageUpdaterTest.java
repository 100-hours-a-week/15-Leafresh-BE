package ktb.leafresh.backend.domain.challenge.group.application.service.updater;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeExampleImage;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeExampleImageRepository;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeUpdateRequestDto;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.util.image.ImageEntityUpdater;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("GroupChallengeExampleImageUpdater 테스트")
@ExtendWith(MockitoExtension.class)
class GroupChallengeExampleImageUpdaterTest {

    @Mock
    private GroupChallengeExampleImageRepository imageRepository;

    @Mock
    private ImageEntityUpdater imageEntityUpdater;

    @InjectMocks
    private GroupChallengeExampleImageUpdater updater;

    private GroupChallenge challenge;
    private Member member;
    private GroupChallengeExampleImage existingImage;

    @BeforeEach
    void setUp() {
        member = MemberFixture.of();
        ReflectionTestUtils.setField(member, "id", 100L);

        GroupChallengeCategory category = GroupChallengeCategoryFixture.defaultCategory();
        challenge = GroupChallengeFixture.of(member, category);

        existingImage = GroupChallengeExampleImage.of(challenge, "https://image.com/existing.png",
                ExampleImageType.SUCCESS, "기존 이미지", 1);
        ReflectionTestUtils.setField(existingImage, "id", 1L);
    }

    @Test
    @DisplayName("예시 이미지 목록을 정상적으로 업데이트할 수 있다")
    void updateImages_withValidInput_updatesImagesSuccessfully() {
        // given
        var keepImage = new GroupChallengeUpdateRequestDto.ExampleImages.KeepImage(1L, 2);
        var newImage = new GroupChallengeUpdateRequestDto.ExampleImages.NewImage(
                "https://image.com/new.png", ExampleImageType.FAILURE, "새 이미지", 3
        );
        var deletedIds = List.of(5L);

        GroupChallengeUpdateRequestDto.ExampleImages exampleImages =
                new GroupChallengeUpdateRequestDto.ExampleImages(
                        List.of(keepImage),
                        List.of(newImage),
                        deletedIds
                );

        given(imageRepository.findById(1L)).willReturn(Optional.of(existingImage));
        given(imageRepository.findById(5L)).willReturn(Optional.of(existingImage)); // 같은 owner로 간주

        // when
        updater.updateImages(challenge, exampleImages);

        // then
        verify(imageEntityUpdater).update(eq(challenge),
                eq(List.of(new ImageEntityUpdater.KeepImage(1L, 2))),
                argThat(newEntities -> newEntities.size() == 1 &&
                        newEntities.get(0).getImageUrl().equals("https://image.com/new.png")),
                eq(deletedIds),
                eq(imageRepository)
        );
    }

    @Test
    @DisplayName("keep 이미지의 소유자가 다르면 예외가 발생한다")
    void updateImages_keepImageOwnedByOther_throwsException() {
        // given
        Member another = MemberFixture.of("other@leafresh.com", "다른사람");
        ReflectionTestUtils.setField(another, "id", 200L);

        GroupChallenge otherChallenge = GroupChallengeFixture.of(another, GroupChallengeCategoryFixture.defaultCategory());
        GroupChallengeExampleImage otherImage = GroupChallengeExampleImage.of(otherChallenge, "url", ExampleImageType.SUCCESS, "desc", 1);
        ReflectionTestUtils.setField(otherImage, "id", 99L);

        GroupChallengeUpdateRequestDto.ExampleImages exampleImages =
                new GroupChallengeUpdateRequestDto.ExampleImages(
                        List.of(new GroupChallengeUpdateRequestDto.ExampleImages.KeepImage(99L, 1)),
                        List.of(),
                        List.of()
                );

        given(imageRepository.findById(99L)).willReturn(Optional.of(otherImage));

        // when & then
        assertThatThrownBy(() -> updater.updateImages(challenge, exampleImages))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.CHALLENGE_UPDATE_IMAGE_PERMISSION_DENIED.getMessage());
    }

    @Test
    @DisplayName("deleted 이미지가 존재하지 않으면 예외가 발생한다")
    void updateImages_deletedImageNotFound_throwsException() {
        // given
        GroupChallengeUpdateRequestDto.ExampleImages exampleImages =
                new GroupChallengeUpdateRequestDto.ExampleImages(
                        List.of(),
                        List.of(),
                        List.of(404L)
                );

        given(imageRepository.findById(404L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> updater.updateImages(challenge, exampleImages))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.GROUP_CHALLENGE_NOT_FOUND.getMessage());
    }
}
