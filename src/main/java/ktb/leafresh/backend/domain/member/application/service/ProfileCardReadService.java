package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeParticipantRecord;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.TreeLevelRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.ProfileCardResponseDto;
import ktb.leafresh.backend.domain.member.presentation.dto.response.ProfileCardResponseDto.RecentBadgeDto;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileCardReadService {

    private final MemberRepository memberRepository;
    private final MemberBadgeRepository memberBadgeRepository;
    private final TreeLevelRepository treeLevelRepository;

    public ProfileCardResponseDto getProfileCard(Long memberId) {
        try {
            log.info("[1] 프로필 카드 조회 시작 - memberId: {}", memberId);
            Member member = findMember(memberId);
            TreeLevel current = member.getTreeLevel();
            TreeLevel next = findNextTreeLevel(current);

            int totalSuccess = countTotalSuccess(member);
            int completedGroupCount = countCompletedGroupChallenges(member);
            List<RecentBadgeDto> badges = fetchRecentBadges(memberId);

            int leafPointsToNextLevel = (next != null)
                    ? next.getMinLeafPoint() - member.getTotalLeafPoints()
                    : 0;

            log.info("[6] 프로필 카드 응답 생성 완료 - memberId: {}", memberId);
            return buildResponse(member, current, next, totalSuccess, completedGroupCount, badges, leafPointsToNextLevel);

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[X] 예기치 못한 오류 발생 - memberId: {}, message: {}", memberId, e.getMessage(), e);
            throw new CustomException(MemberErrorCode.PROFILE_CARD_QUERY_FAILED);
        }
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.warn("[1-1] 회원을 찾을 수 없습니다 - memberId: {}", memberId);
                    return new CustomException(MemberErrorCode.PROFILE_CARD_NOT_FOUND);
                });
    }

    private TreeLevel findNextTreeLevel(TreeLevel current) {
        TreeLevel next = treeLevelRepository
                .findFirstByMinLeafPointGreaterThanOrderByMinLeafPointAsc(current.getMinLeafPoint())
                .orElse(null);

        if (next != null) {
            log.debug("[2-2] 다음 트리 레벨: {} (minPoint: {})", next.getName(), next.getMinLeafPoint());
        } else {
            log.debug("[2-2] 다음 트리 레벨 없음 (최종 단계)");
        }
        return next;
    }

    private int countTotalSuccess(Member member) {
        int group = (int) member.getGroupChallengeParticipantRecords().stream()
                .flatMap(r -> r.getVerifications().stream())
                .filter(v -> v.getStatus() == ChallengeStatus.SUCCESS)
                .count();

        int personal = (int) member.getPersonalChallengeVerifications().stream()
                .filter(v -> v.getStatus() == ChallengeStatus.SUCCESS)
                .count();

        log.debug("[3] 총 인증 성공 횟수 - 개인: {}, 단체: {}, 합계: {}", personal, group, group + personal);
        return group + personal;
    }

    private int countCompletedGroupChallenges(Member member) {
        int count = (int) member.getGroupChallengeParticipantRecords().stream()
                .filter(GroupChallengeParticipantRecord::isAllSuccess)
                .count();
        log.debug("[4] 단체 챌린지 완주 횟수: {}", count);
        return count;
    }

    private List<RecentBadgeDto> fetchRecentBadges(Long memberId) {
        List<RecentBadgeDto> result = memberBadgeRepository.findRecentBadgesByMemberId(memberId, 3)
                .stream()
                .map(b -> new RecentBadgeDto(
                        b.getBadge().getId(),
                        b.getBadge().getName(),
                        b.getBadge().getImageUrl()))
                .toList();
        log.debug("[5] 최근 획득 뱃지 개수: {}", result.size());
        return result;
    }

    private ProfileCardResponseDto buildResponse(
            Member member,
            TreeLevel current,
            TreeLevel next,
            int totalSuccess,
            int completedGroupCount,
            List<RecentBadgeDto> badges,
            int leafPointsToNextLevel
    ) {
        return ProfileCardResponseDto.builder()
                .nickname(member.getNickname())
                .profileImageUrl(member.getImageUrl())
                .treeLevelId(current.getId())
                .treeLevelName(current.getName().name())
                .treeImageUrl(current.getImageUrl())
                .nextTreeLevelName(next != null ? next.getName().name() : null)
                .nextTreeImageUrl(next != null ? next.getImageUrl() : null)
                .totalLeafPoints(member.getTotalLeafPoints())
                .leafPointsToNextLevel(leafPointsToNextLevel)
                .totalSuccessfulVerifications(totalSuccess)
                .completedGroupChallengesCount(completedGroupCount)
                .badges(badges)
                .build();
    }
}
