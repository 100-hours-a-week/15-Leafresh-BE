package ktb.leafresh.backend.domain.store.order.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

  private final MemberRepository memberRepository;

  @Override
  public boolean hasEnoughPoints(Long memberId, int required) {
    return memberRepository
        .findById(memberId)
        .map(Member::getCurrentLeafPoints) // Integer 타입이어야 함
        .map(p -> p >= required)
        .orElse(false);
  }
}
