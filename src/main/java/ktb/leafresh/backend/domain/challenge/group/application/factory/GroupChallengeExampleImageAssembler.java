package ktb.leafresh.backend.domain.challenge.group.application.factory;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeExampleImage;
import ktb.leafresh.backend.domain.challenge.group.presentation.dto.request.GroupChallengeCreateRequestDto;
import org.springframework.stereotype.Component;

@Component
public class GroupChallengeExampleImageAssembler {

    public void assemble(GroupChallenge challenge, GroupChallengeCreateRequestDto dto) {
        dto.exampleImages().forEach(imageDto -> {
            // 정적 팩토리 메서드로 생성 (연관관계는 내부에서 set만 함)
            GroupChallengeExampleImage image = GroupChallengeExampleImage.of(
                    challenge,
                    imageDto.imageUrl(),
                    imageDto.type(),
                    imageDto.description(),
                    imageDto.sequenceNumber()
            );

            // 양방향 연결을 명시적으로 처리
            challenge.addExampleImage(image);
        });
    }
}
