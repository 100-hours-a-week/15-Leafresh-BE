package ktb.leafresh.backend.domain.challenge.group.domain.service;

import java.time.LocalDate;

public class GroupChallengeRemainingDayCalculator {

  public static int calculate(LocalDate startDate) {
    LocalDate today = LocalDate.now();

    if (!startDate.isAfter(today)) {
      return 0;
    }
    return (int) java.time.temporal.ChronoUnit.DAYS.between(today, startDate);
  }
}
