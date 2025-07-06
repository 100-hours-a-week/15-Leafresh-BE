package ktb.leafresh.backend.domain.challenge.group.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

class GroupChallengeRemainingDayCalculatorTest {

    @Test
    @DisplayName("시작일이 오늘과 같거나 이전이면 남은 일수는 0이다")
    void calculate_withTodayOrPastDate_returnsZero() {
        // given
        LocalDate today = LocalDate.of(2025, 7, 5);

        try (MockedStatic<LocalDate> mocked = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(today);

            // when & then
            assertThat(GroupChallengeRemainingDayCalculator.calculate(today)).isZero();
            assertThat(GroupChallengeRemainingDayCalculator.calculate(today.minusDays(1))).isZero();
        }
    }

    @Test
    @DisplayName("시작일이 오늘 이후라면 남은 일수를 반환한다")
    void calculate_withFutureDate_returnsDaysBetweenTodayAndStartDate() {
        // given
        LocalDate today = LocalDate.of(2025, 7, 5);
        LocalDate futureDate = today.plusDays(3);

        try (MockedStatic<LocalDate> mocked = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
            mocked.when(LocalDate::now).thenReturn(today);

            // when
            int remainingDays = GroupChallengeRemainingDayCalculator.calculate(futureDate);

            // then
            assertThat(remainingDays).isEqualTo(3);
        }
    }
}
