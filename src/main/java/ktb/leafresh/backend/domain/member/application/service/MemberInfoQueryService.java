package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.presentation.dto.response.MemberInfoResponseDto;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberInfoQueryService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public MemberInfoResponseDto getMemberInfo(Long memberId) {
        log.debug("[회원 정보 조회] 요청 시작 - memberId: {}", memberId);

        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_FOUND));

            TreeLevel treeLevel = member.getTreeLevel();
            if (treeLevel == null) {
                log.error("[회원 정보 조회] 트리 레벨 정보 없음 - memberId: {}", memberId);
                throw new CustomException(MemberErrorCode.TREE_LEVEL_NOT_FOUND);
            }

            return MemberInfoResponseDto.of(member, treeLevel);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[회원 정보 조회] 서비스 내부 오류", e);
            throw new CustomException(MemberErrorCode.MEMBER_INFO_QUERY_FAILED);
        }
    }
}
