package ktb.leafresh.backend.domain.verification.application.service;

import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerificationCountQueryService {

  private final GroupChallengeVerificationRepository groupRepo;
  private final PersonalChallengeVerificationRepository personalRepo;

  public int getTotalVerificationCountFromDB() {
    return groupRepo.countAll() + personalRepo.countAll();
  }
}
