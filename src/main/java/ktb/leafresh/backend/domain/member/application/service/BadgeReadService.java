package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.enums.BadgeType;
import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.BadgeListResponseDto;
import ktb.leafresh.backend.domain.member.presentation.dto.response.BadgeResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeReadService {

    private final BadgeRepository badgeRepository;
    private final MemberRepository memberRepository;

    @Value("${lock-image-url}")
    private String lockImageUrl;

    public BadgeListResponseDto getAllBadges(Long memberId) {
        log.debug("[뱃지 목록 조회] 요청 시작 - memberId: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("[뱃지 목록 조회] 존재하지 않는 회원 - memberId: {}", memberId);
                    return new CustomException(MemberErrorCode.MEMBER_NOT_FOUND); // 404
                });

        try {
            List<Badge> allBadges = badgeRepository.findAll();
            if (allBadges.isEmpty()) {
                log.warn("[뱃지 목록 조회] 뱃지 데이터 없음 - memberId: {}", memberId);
                throw new CustomException(MemberErrorCode.BADGE_QUERY_FAILED); // 500
            }

            Set<Long> acquiredBadgeIds = member.getMemberBadges().stream()
                    .map(memberBadge -> memberBadge.getBadge().getId())
                    .collect(Collectors.toSet());

            Map<BadgeType, List<BadgeResponseDto>> grouped = new EnumMap<>(BadgeType.class);
            for (Badge badge : allBadges) {
                boolean isLocked = !acquiredBadgeIds.contains(badge.getId());

                BadgeResponseDto dto = BadgeResponseDto.of(badge, isLocked, lockImageUrl);
                grouped.computeIfAbsent(badge.getType(), k -> new ArrayList<>()).add(dto);
            }

            log.debug("[뱃지 목록 조회] 성공 - memberId: {}", memberId);
            return BadgeListResponseDto.from(grouped);

        } catch (CustomException e) {
            throw e;

        } catch (Exception e) {
            log.error("[뱃지 목록 조회] 처리 중 알 수 없는 오류 발생", e);
            throw new CustomException(MemberErrorCode.BADGE_QUERY_FAILED); // 500
        }
    }
}
