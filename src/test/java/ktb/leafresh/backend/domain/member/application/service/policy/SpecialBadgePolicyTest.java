//package ktb.leafresh.backend.domain.member.application.service.policy;
//
//import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
//import ktb.leafresh.backend.domain.challenge.group.domain.entity.enums.GroupChallengeCategoryName;
//import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
//import ktb.leafresh.backend.domain.member.domain.entity.Badge;
//import ktb.leafresh.backend.domain.member.domain.entity.Member;
//import ktb.leafresh.backend.domain.member.domain.entity.enums.BadgeType;
//import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
//import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberBadgeRepository;
//import ktb.leafresh.backend.domain.verification.infrastructure.repository.GroupChallengeVerificationRepository;
//import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
//import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
//import ktb.leafresh.backend.support.fixture.BadgeFixture;
//import ktb.leafresh.backend.support.fixture.MemberFixture;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("SpecialBadgePolicy 테스트")
//class SpecialBadgePolicyTest {
//
//    @Mock
//    private GroupChallengeVerificationRepository groupChallengeVerificationRepository;
//
//    @Mock
//    private PersonalChallengeVerificationRepository personalChallengeVerificationRepository;
//
//    @Mock
//    private BadgeRepository badgeRepository;
//
//    @Mock
//    private MemberBadgeRepository memberBadgeRepository;
//
//    @Mock
//    private GroupChallengeCategoryRepository groupChallengeCategoryRepository;
//
//    @InjectMocks
//    private SpecialBadgePolicy policy;
//
//    private Member member;
//
//    @BeforeEach
//    void setUp() {
//        member = MemberFixture.of();
//    }
//
//    @Test
//    @DisplayName("모든 그룹 챌린지 카테고리 인증 성공 시 '지속가능 전도사' 뱃지 지급")
//    void evaluateAllGroupCategoriesCleared_withSuccess_returnsSustainabilityBadge() {
//        // given
//        Badge expectedBadge = BadgeFixture.of("지속가능 전도사", BadgeType.SPECIAL);
//
//        for (GroupChallengeCategoryName category : GroupChallengeCategoryName.values()) {
//            if (category == GroupChallengeCategoryName.ETC) continue;
//
//            GroupChallengeCategory categoryEntity = mock(GroupChallengeCategory.class);
//            when(groupChallengeCategoryRepository.findByName(category.name()))
//                    .thenReturn(Optional.of(categoryEntity));
//            when(groupChallengeVerificationRepository.existsByMemberIdAndCategoryAndStatus(
//                    member.getId(), categoryEntity, ChallengeStatus.SUCCESS)).thenReturn(true);
//        }
//
//        when(badgeRepository.findByName(expectedBadge.getName()))
//                .thenReturn(Optional.of(expectedBadge));
//        when(memberBadgeRepository.existsByMemberAndBadge(member, expectedBadge)).thenReturn(false);
//
//        // when
//        List<Badge> result = policy.evaluateAndGetNewBadges(member);
//
//        // then
//        assertThat(result).containsExactly(expectedBadge);
//    }
//
//    @Test
//    @DisplayName("모든 개인 챌린지 인증 성공 시 '도전 전부러' 뱃지 지급")
//    void evaluateAllPersonalChallengesCleared_withSuccess_returnsChallengeClearBadge() {
//        // given
//        Badge expectedBadge = BadgeFixture.of("도전 전부러", BadgeType.SPECIAL);
//        List<String> titles = List.of(
//                "텀블러 사용하기", "에코백 사용하기", "장바구니 사용하기", "자전거 타기",
//                "대중교통 이용하기", "샐러드/채식 식단 먹기", "음식 남기지 않기", "계단 이용하기",
//                "재활용 분리수거하기", "손수건 사용하기", "쓰레기 줍기", "안 쓰는 전기 플러그 뽑기",
//                "고체 비누 사용하기", "하루 만 보 걷기", "도시락 싸먹기", "작은 텃밭 가꾸기",
//                "반려 식물 인증", "전자 영수증 받기", "친환경 인증 마크 상품 구매하기",
//                "다회용기 사용하기", "책·전자책 읽기 인증하기"
//        );
//
//        when(personalChallengeVerificationRepository.findAllPersonalChallengeTitles()).thenReturn(titles);
//        titles.forEach(title -> when(personalChallengeVerificationRepository.existsByMemberIdAndPersonalChallengeTitleAndStatus(
//                member.getId(), title, ChallengeStatus.SUCCESS)).thenReturn(true)
//        );
//
//        when(badgeRepository.findByName(expectedBadge.getName()))
//                .thenReturn(Optional.of(expectedBadge));
//        when(memberBadgeRepository.existsByMemberAndBadge(member, expectedBadge)).thenReturn(false);
//
//        // when
//        List<Badge> result = policy.evaluateAndGetNewBadges(member);
//
//        // then
//        assertThat(result).containsExactly(expectedBadge);
//    }
//
//    @Test
//    @DisplayName("특정 단체 카테고리에서 10회 인증 성공 시 마스터 뱃지 지급")
//    void evaluateGroupCategoryMaster_with10Success_returnsMasterBadge() {
//        // given
//        GroupChallengeCategoryName categoryEnum = GroupChallengeCategoryName.PLOGGING;
//        String badgeName = categoryEnum.getLabel() + " 마스터";
//        Badge expectedBadge = BadgeFixture.of(badgeName, BadgeType.SPECIAL);
//        GroupChallengeCategory category = mock(GroupChallengeCategory.class);
//
//        when(groupChallengeCategoryRepository.findByName(categoryEnum.name())).thenReturn(Optional.of(category));
//        when(groupChallengeVerificationRepository.countByMemberIdAndCategoryAndStatus(member.getId(), category, ChallengeStatus.SUCCESS)).thenReturn(10L);
//        when(badgeRepository.findByName(expectedBadge.getName())).thenReturn(Optional.of(expectedBadge));
//        when(memberBadgeRepository.existsByMemberAndBadge(member, expectedBadge)).thenReturn(false);
//
//        // when
//        List<Badge> result = policy.evaluateAndGetNewBadges(member);
//
//        // then
//        assertThat(result).contains(expectedBadge);
//    }
//
//    @Test
//    @DisplayName("30일 연속 인증 성공 시 '에코 슈퍼루키' 뱃지 지급")
//    void evaluateConsecutive30Days_withSuccess_returnsEcoSuperRookieBadge() {
//        // given
//        Badge expectedBadge = BadgeFixture.of("에코 슈퍼루키", BadgeType.SPECIAL);
//
//        when(personalChallengeVerificationRepository.countConsecutiveSuccessDays(member.getId())).thenReturn(30);
//        when(badgeRepository.findByName(expectedBadge.getName())).thenReturn(Optional.of(expectedBadge));
//        when(memberBadgeRepository.existsByMemberAndBadge(member, expectedBadge)).thenReturn(false);
//
//        // when
//        List<Badge> result = policy.evaluateAndGetNewBadges(member);
//
//        // then
//        assertThat(result).contains(expectedBadge);
//    }
//}
