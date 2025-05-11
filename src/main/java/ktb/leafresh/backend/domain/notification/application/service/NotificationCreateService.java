package ktb.leafresh.backend.domain.notification.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.notification.domain.entity.Notification;
import ktb.leafresh.backend.domain.notification.domain.entity.enums.NotificationType;
import ktb.leafresh.backend.domain.notification.infrastructure.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationCreateService {

    private final NotificationRepository notificationRepository;

    public void createChallengeVerificationResultNotification(
            Member member,
            String challengeName,
            boolean isSuccess,
            NotificationType type,
            Long challengeId
    ) {
        String titlePrefix = (type == NotificationType.PERSONAL) ? "[개인]" : "[단체]";
        String title = titlePrefix + " " + challengeName + " 인증 심사 결과가 도착했습니다.";
        String content = isSuccess ? "인증이 승인되었습니다." : "반려되었습니다.";

        Notification notification = Notification.builder()
                .member(member)
                .title(title)
                .content(content)
                .type(type)
                .challengeId(challengeId)
                .build();

        notificationRepository.save(notification);
    }
}
