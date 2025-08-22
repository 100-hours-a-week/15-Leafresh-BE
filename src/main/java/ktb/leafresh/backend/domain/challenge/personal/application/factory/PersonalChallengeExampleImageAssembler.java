package ktb.leafresh.backend.domain.challenge.personal.application.factory;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallengeExampleImage;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.request.PersonalChallengeCreateRequestDto;
import org.springframework.stereotype.Component;

@Component
public class PersonalChallengeExampleImageAssembler {

  public void assemble(PersonalChallenge challenge, PersonalChallengeCreateRequestDto dto) {
    dto.exampleImages()
        .forEach(
            imageDto -> {
              // 정적 팩토리 메서드로 생성 (연관관계는 내부에서 set만 함)
              PersonalChallengeExampleImage image =
                  PersonalChallengeExampleImage.of(
                      challenge,
                      imageDto.imageUrl(),
                      imageDto.type(),
                      imageDto.description(),
                      imageDto.sequenceNumber());

              // 양방향 연결을 명시적으로 처리
              challenge.addExampleImage(image);
            });
  }
}
