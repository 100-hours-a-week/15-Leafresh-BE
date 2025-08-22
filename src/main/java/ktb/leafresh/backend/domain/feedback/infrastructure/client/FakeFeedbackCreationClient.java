package ktb.leafresh.backend.domain.feedback.infrastructure.client;

import ktb.leafresh.backend.domain.feedback.infrastructure.dto.request.AiFeedbackCreationRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
public class FakeFeedbackCreationClient implements FeedbackCreationClient {

  @Override
  public void requestWeeklyFeedback(AiFeedbackCreationRequestDto requestDto) {
    log.info("[FAKE 피드백 요청] 로컬 환경에서는 요청을 전송하지 않습니다. 요청 DTO: {}", requestDto);
  }
}
