package ktb.leafresh.backend.domain.challenge.personal.application.service;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.challenge.personal.infrastructure.repository.PersonalChallengeRepository;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response.PersonalChallengeListResponseDto;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response.PersonalChallengeSummaryDto;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonalChallengeReadService {

    private final PersonalChallengeRepository repository;

    public PersonalChallengeListResponseDto getByDayOfWeek(DayOfWeek dayOfWeek) {
        List<PersonalChallenge> challenges = repository.findAllByDayOfWeek(dayOfWeek);

        if (challenges.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND, "현재 등록된 개인 챌린지가 없습니다.");
        }

        return new PersonalChallengeListResponseDto(PersonalChallengeSummaryDto.fromEntities(challenges));
    }
}
