package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.MemberBadge;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.RecentBadgeListResponseDto;
import ktb.leafresh.backend.domain.member.presentation.dto.response.BadgeSummaryDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecentBadgeReadService {

    private final MemberRepository memberRepository;
    private final MemberBadgeRepository memberBadgeRepository;

    public RecentBadgeListResponseDto getRecentBadges(Long memberId, int count) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("[최근 뱃지 조회] 존재하지 않는 사용자 - memberId: {}", memberId);
                    throw new CustomException(MemberErrorCode.MEMBER_NOT_FOUND);
                });

        try {
            List<MemberBadge> recentBadges = memberBadgeRepository.findRecentBadgesByMemberId(memberId, count);

            log.info("[최근 뱃지 조회] 조회 성공 - memberId: {}, count: {}", memberId, recentBadges.size());

            List<BadgeSummaryDto> badgeDtos = recentBadges.stream()
                    .map(b -> BadgeSummaryDto.builder()
                            .id(b.getBadge().getId())
                            .name(b.getBadge().getName())
                            .condition(b.getBadge().getCondition())
                            .imageUrl(b.getBadge().getImageUrl())
                            .build())
                    .toList();

            return RecentBadgeListResponseDto.builder()
                    .badges(badgeDtos)
                    .build();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[최근 뱃지 조회] 서버 내부 오류 발생 - memberId: {}", memberId, e);
            throw new CustomException(MemberErrorCode.BADGE_QUERY_FAILED);
        }
    }
}
