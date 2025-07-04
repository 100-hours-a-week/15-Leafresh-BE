package ktb.leafresh.backend.domain.challenge.personal.application.validator;

import ktb.leafresh.backend.domain.challenge.personal.infrastructure.repository.PersonalChallengeRepository;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonalChallengeDomainValidatorTest {

    @Mock
    private PersonalChallengeRepository repository;

    @InjectMocks
    private PersonalChallengeDomainValidator validator;

    @Nested
    @DisplayName("validate()는")
    class Validate {

        @Test
        @DisplayName("요일별 개인 챌린지가 3개 미만이면 예외를 던지지 않는다")
        void doesNotThrowException_whenCountIsLessThan3() {
            // given
            DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
            given(repository.countByDayOfWeek(dayOfWeek)).willReturn(2);

            // when & then
            assertThatCode(() -> validator.validate(dayOfWeek))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("요일별 개인 챌린지가 3개 이상이면 예외를 던진다")
        void throwsException_whenCountIs3OrMore() {
            // given
            DayOfWeek dayOfWeek = DayOfWeek.FRIDAY;
            given(repository.countByDayOfWeek(dayOfWeek)).willReturn(3);

            // when & then
            assertThatThrownBy(() -> validator.validate(dayOfWeek))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(ChallengeErrorCode.EXCEEDS_DAILY_PERSONAL_CHALLENGE_LIMIT.getMessage());
        }
    }
}
