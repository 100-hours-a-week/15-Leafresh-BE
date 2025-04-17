package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.MemberLeafPointResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberLeafPointReadService {

    private final MemberRepository memberRepository;

    public MemberLeafPointResponseDto getCurrentLeafPoints(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("[나뭇잎 보유량 조회] 존재하지 않는 사용자 - memberId: {}", memberId);
                    return new CustomException(MemberErrorCode.MEMBER_NOT_FOUND);
                });

        Integer leafPoints = member.getCurrentLeafPoints();
        log.info("[나뭇잎 보유량 조회] 조회 성공 - memberId: {}, leafPoints: {}", memberId, leafPoints);

        return new MemberLeafPointResponseDto(leafPoints);
    }
}
