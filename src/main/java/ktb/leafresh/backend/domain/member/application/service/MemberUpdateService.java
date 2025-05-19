package ktb.leafresh.backend.domain.member.application.service;

import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.global.exception.GlobalErrorCode;
import ktb.leafresh.backend.global.exception.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberUpdateService {

    private final MemberRepository memberRepository;

    @Transactional
    public void updateMemberInfo(Member member, String newNickname, String newImageUrl) {
        boolean updated = false;

        log.debug("[회원 정보 수정] 시작 - memberId: {}", member.getId());

        try {
            if (newNickname != null && !newNickname.equals(member.getNickname())) {
                validateNicknameFormat(newNickname);

                if (memberRepository.existsByNickname(newNickname)) {
                    throw new CustomException(MemberErrorCode.ALREADY_EXISTS);
                }

                member.updateNickname(newNickname);
                updated = true;
                log.info("[회원 정보 수정] 닉네임 변경: {}", newNickname);
            }

            if (newImageUrl != null && !newImageUrl.equals(member.getImageUrl())) {
                member.updateImageUrl(newImageUrl);
                updated = true;
                log.info("[회원 정보 수정] 이미지 URL 변경");
            }

            if (!updated) {
                throw new CustomException(GlobalErrorCode.NO_CONTENT);
            }

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("[회원 정보 수정] 서버 오류 발생", e);
            throw new CustomException(MemberErrorCode.NICKNAME_UPDATE_FAILED);
        }
    }

    private void validateNicknameFormat(String nickname) {
        if (!nickname.matches("^[a-zA-Z0-9가-힣]{1,20}$")) {
            throw new CustomException(MemberErrorCode.NICKNAME_INVALID_FORMAT);
        }
    }
}
