package ktb.leafresh.backend.domain.challenge.group.application.factory;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeExampleImage;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeCreateRequestDto;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import ktb.leafresh.backend.support.fixture.GroupChallengeCategoryFixture;
import ktb.leafresh.backend.support.fixture.GroupChallengeFixture;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupChallengeExampleImageAssembler 테스트")
class GroupChallengeExampleImageAssemblerTest {

    private GroupChallengeExampleImageAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new GroupChallengeExampleImageAssembler();
    }

    @Test
    @DisplayName("예시 이미지 DTO를 GroupChallenge에 매핑하고 양방향 연관관계를 설정한다")
    void assemble_withValidDto_setsImagesAndRelationships() {
        // given
        Member member = MemberFixture.of();
        GroupChallengeCategory category = GroupChallengeCategoryFixture.defaultCategory();
        GroupChallenge challenge = GroupChallengeFixture.of(member, category);
        ReflectionTestUtils.setField(challenge, "exampleImages", new ArrayList<>());

        GroupChallengeCreateRequestDto.ExampleImageRequestDto imageDto1 =
                new GroupChallengeCreateRequestDto.ExampleImageRequestDto(
                        "https://image1.jpg", ExampleImageType.SUCCESS, "성공 예시1", 1);

        GroupChallengeCreateRequestDto.ExampleImageRequestDto imageDto2 =
                new GroupChallengeCreateRequestDto.ExampleImageRequestDto(
                        "https://image2.jpg", ExampleImageType.FAILURE, "실패 예시1", 2);

        GroupChallengeCreateRequestDto dto = new GroupChallengeCreateRequestDto(
                "제로웨이스트 챌린지",
                "일회용품 줄이기",
                "ZERO_WASTE",
                200,
                "https://thumbnail.jpg",
                OffsetDateTime.now(),
                OffsetDateTime.now().plusDays(7),
                LocalTime.of(6, 0),
                LocalTime.of(22, 0),
                List.of(imageDto1, imageDto2)
        );

        // when
        assembler.assemble(challenge, dto);

        // then
        List<GroupChallengeExampleImage> exampleImages = challenge.getExampleImages();

        assertThat(exampleImages).hasSize(2);

        assertThat(exampleImages).anySatisfy(image -> {
            assertThat(image.getImageUrl()).isEqualTo("https://image1.jpg");
            assertThat(image.getType()).isEqualTo(ExampleImageType.SUCCESS);
            assertThat(image.getDescription()).isEqualTo("성공 예시1");
            assertThat(image.getSequenceNumber()).isEqualTo(1);
            assertThat(image.getGroupChallenge()).isEqualTo(challenge);
        });

        assertThat(exampleImages).anySatisfy(image -> {
            assertThat(image.getImageUrl()).isEqualTo("https://image2.jpg");
            assertThat(image.getType()).isEqualTo(ExampleImageType.FAILURE);
            assertThat(image.getDescription()).isEqualTo("실패 예시1");
            assertThat(image.getSequenceNumber()).isEqualTo(2);
            assertThat(image.getGroupChallenge()).isEqualTo(challenge);
        });
    }
}
