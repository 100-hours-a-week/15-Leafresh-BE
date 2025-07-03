package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationCountQueryServiceTest {

    @Mock
    private GroupChallengeVerificationRepository groupRepo;

    @Mock
    private PersonalChallengeVerificationRepository personalRepo;

    @InjectMocks
    private VerificationCountQueryService queryService;

    @Test
    @DisplayName("전체 인증 수 조회 - 그룹 + 개인 인증 수의 합 반환")
    void getTotalVerificationCountFromDB_returnsSum() {
        // given
        given(groupRepo.countAll()).willReturn(120);
        given(personalRepo.countAll()).willReturn(80);

        // when
        int result = queryService.getTotalVerificationCountFromDB();

        // then
        assertThat(result).isEqualTo(200);
        verify(groupRepo).countAll();
        verify(personalRepo).countAll();
    }
}
