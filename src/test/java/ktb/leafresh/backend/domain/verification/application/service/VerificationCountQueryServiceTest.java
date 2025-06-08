package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class VerificationCountQueryServiceTest {

    private GroupChallengeVerificationRepository groupRepo;
    private PersonalChallengeVerificationRepository personalRepo;
    private VerificationCountQueryService service;

    @BeforeEach
    void setUp() {
        groupRepo = mock(GroupChallengeVerificationRepository.class);
        personalRepo = mock(PersonalChallengeVerificationRepository.class);
        service = new VerificationCountQueryService(groupRepo, personalRepo);
    }

    @Test
    @DisplayName("DB에 저장된 그룹/개인 인증 수를 합산하여 반환한다")
    void getTotalVerificationCountFromDB_success() {
        // given
        when(groupRepo.countAll()).thenReturn(8);
        when(personalRepo.countAll()).thenReturn(12);

        // when
        int totalCount = service.getTotalVerificationCountFromDB();

        // then
        assertThat(totalCount).isEqualTo(20);
        verify(groupRepo, times(1)).countAll();
        verify(personalRepo, times(1)).countAll();
    }

    @Test
    @DisplayName("DB 쿼리 중 예외가 발생하면 예외가 그대로 던져진다")
    void getTotalVerificationCountFromDB_fail_dueToException() {
        // given
        when(groupRepo.countAll()).thenThrow(new RuntimeException("DB 오류"));

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            service.getTotalVerificationCountFromDB();
        });

        verify(groupRepo).countAll();
        verifyNoInteractions(personalRepo); // groupRepo에서 실패 시 personalRepo는 호출되지 않아야 함
    }
}
