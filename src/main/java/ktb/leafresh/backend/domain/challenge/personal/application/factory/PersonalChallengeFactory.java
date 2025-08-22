package ktb.leafresh.backend.domain.challenge.personal.application.factory;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.request.PersonalChallengeCreateRequestDto;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class PersonalChallengeFactory {

  public PersonalChallenge create(PersonalChallengeCreateRequestDto dto) {
    return PersonalChallenge.builder()
        .title(dto.title())
        .description(dto.description())
        .dayOfWeek(dto.dayOfWeek())
        .imageUrl(dto.thumbnailImageUrl())
        .verificationStartTime(dto.verificationStartTime())
        .verificationEndTime(dto.verificationEndTime())
        .leafReward(200)
        .build();
  }
}
