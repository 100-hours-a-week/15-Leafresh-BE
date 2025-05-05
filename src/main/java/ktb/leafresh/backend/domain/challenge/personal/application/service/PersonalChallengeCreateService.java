package ktb.leafresh.backend.domain.challenge.personal.application.service;

import ktb.leafresh.backend.domain.challenge.personal.application.factory.PersonalChallengeFactory;
import ktb.leafresh.backend.domain.challenge.personal.application.validator.PersonalChallengeDomainValidator;
import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.challenge.personal.infrastructure.repository.PersonalChallengeRepository;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.request.PersonalChallengeCreateRequestDto;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response.PersonalChallengeCreateResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonalChallengeCreateService {

    private final PersonalChallengeDomainValidator validator;
    private final PersonalChallengeFactory factory;
    private final PersonalChallengeRepository repository;

    @Transactional
    public PersonalChallengeCreateResponseDto create(PersonalChallengeCreateRequestDto dto) {
        validator.validate(dto.dayOfWeek());
        PersonalChallenge challenge = factory.create(dto);
        repository.save(challenge);
        return new PersonalChallengeCreateResponseDto(challenge.getId());
    }
}
