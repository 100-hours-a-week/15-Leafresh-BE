package ktb.leafresh.backend.domain.challenge.group.application.service;

import jakarta.transaction.Transactional;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeExampleImage;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.enums.GroupChallengeCategoryName;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeExampleImageRepository;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.enums.LoginType;
import ktb.leafresh.backend.domain.member.domain.entity.enums.Role;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.TreeLevelRepository;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventChallengeInitService {

    private final GroupChallengeRepository challengeRepository;
    private final GroupChallengeCategoryRepository categoryRepository;
    private final GroupChallengeExampleImageRepository imageRepository;
    private final MemberRepository memberRepository;
    private final TreeLevelRepository treeLevelRepository;

    public void registerCurrentYearEventChallengesIfNotExists() {
        int currentYear = LocalDate.now().getYear();

        // 중복 실행 방지 로직
        String checkTitle = "SNS에 습지 보호 캠페인 알리기 " + currentYear;
        if (challengeRepository.existsByTitleAndEventFlagTrue(checkTitle)) {
            return;
        }

        GroupChallengeCategory etcCategory = categoryRepository.findByName(GroupChallengeCategoryName.ETC.name())
                .orElseThrow(() -> new IllegalStateException("ETC 카테고리가 존재하지 않습니다."));

        Member admin = memberRepository.findByEmail("admin@leafresh.io")
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .email("admin@leafresh.io")
                        .nickname("운영자")
                        .loginType(LoginType.SOCIAL)
                        .role(Role.ADMIN)
                        .imageUrl("https://storage.googleapis.com/leafresh-images/init/user_icon.png")
                        .activated(true)
                        .totalLeafPoints(0)
                        .currentLeafPoints(0)
                        .treeLevel(treeLevelRepository.findById(1L)
                                .orElseThrow(() -> new IllegalStateException("기본 트리 레벨이 존재하지 않습니다.")))
                        .build()));

        List<ChallengeSeed> seeds = getEventChallengeSeeds();

        for (ChallengeSeed seed : seeds) {
            String titleWithYear = seed.title() + " " + currentYear;
            if (challengeRepository.existsByTitleAndEventFlagTrue(titleWithYear)) {
                log.info("이벤트 챌린지 이미 존재함: {}", titleWithYear);
                continue;
            }

            LocalDate start = LocalDate.of(currentYear, seed.startMonth(), seed.startDay());
            LocalDate end = start.plusDays(seed.durationDays() - 1);

            GroupChallenge challenge = challengeRepository.save(
                    GroupChallenge.builder()
                            .member(admin)
                            .category(etcCategory)
                            .imageUrl(seed.thumbnail())
                            .title(titleWithYear)
                            .description(seed.description())
                            .leafReward(200)
                            .startDate(start.atStartOfDay())
                            .endDate(end.atTime(23, 59, 59))
                            .verificationStartTime(LocalTime.of(6, 0))
                            .verificationEndTime(LocalTime.of(23, 0))
                            .maxParticipantCount(10000)
                            .currentParticipantCount(0)
                            .eventFlag(true)
                            .build()
            );

            imageRepository.saveAll(List.of(
                    GroupChallengeExampleImage.of(challenge, seed.success(), ExampleImageType.SUCCESS, "성공 예시입니다.", 1),
                    GroupChallengeExampleImage.of(challenge, seed.fail(), ExampleImageType.FAILURE, "실패 예시입니다.", 2)
            ));
        }
    }

    private List<ChallengeSeed> getEventChallengeSeeds() {
        return List.of(
                new ChallengeSeed(
                        "SNS에 습지 보호 캠페인 알리기",
                        "2월 2일, 세계 습지의 날을 맞아 습지의 소중함을 더 많은 사람들과 나누는 온라인 캠페인에 함께해요. \n" +
                                "습지 파괴의 문제를 알리고 보호의 필요성을 강조하는 게시물을 SNS에 공유해 주세요. \n" +
                                "해시태그와 함께 자신의 생각이나 사진, 기사 링크 등을 올리며 우리의 관심이 자연을 지키는 힘이 될 수 있음을 알려봐요.",
                        "https://storage.googleapis.com/leafresh-images/init/1_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.jpg",
                        "https://storage.googleapis.com/leafresh-images/init/1_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/init/1_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png",
                        2, 2, 7
                ),

                new ChallengeSeed(
                        "해양 정화로 고래를 지켜요",
                        "2월 고래의 날을 맞아 고래가 살아가는 바다를 직접 지켜보는 시간을 가져보세요. \n" +
                                "바다에 떠다니는 플라스틱, 스티로폼, 낚시 쓰레기 등은 고래의 생명을 위협합니다. \n" +
                                "이번 챌린지에서는 바닷가, 하천 주변, 해양공원 등 해양 생태계와 연결된 공간에서 쓰레기를 줍는 봉사 활동에 참여해 주세요. \n" +
                                "고래의 바다를 깨끗하게 만드는 당신의 손길이 큰 변화를 만듭니다.",
                        "https://storage.googleapis.com/leafresh-images/init/2_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/init/2_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/init/2_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png",
                        2, 16, 7
                ),

                new ChallengeSeed(
                        "생명의 물을 지켜요! 생활 속 절수+물길 정화 캠페인",
                        "3월 22일은 생명의 소중한 자원, 물을 되돌아보는 ‘세계 물의 날’입니다.\n" +
                                "전 세계 인구의 6분의 1이 깨끗한 물조차 공급받지 못하는 현실 속에서, 우리는 작은 실천으로 생명을 지킬 수 있습니다.\n" +
                                "이번 챌린지에서는 하천·계곡·수로 정화 활동과 함께, 물 절약 실천도 함께 인증해 주세요.\n" +
                                "단체가 함께 모여 지역 물가를 정화하거나, 가정과 학교·직장에서 실천할 수 있는 물 절약 행동을 함께 이어가요.",
                        "https://storage.googleapis.com/leafresh-images/init/3_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/init/3_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/init/3_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png",
                        3, 22, 7
                ),

                new ChallengeSeed(
                        "오늘 내가 심은 나무 한 그루",
                        "4월 5일은 식목일!\n" +
                                "지구의 허파인 나무는 이산화탄소를 줄이고, 생태계를 회복시키는 가장 강력한 친구입니다.\n" +
                                "오늘 하루, 작은 나무 한 그루를 직접 심어보세요. 화분에 씨앗을 심어도 좋고, 단체로 묘목을 심는 활동도 좋아요.\n" +
                                "내가 심은 작은 생명이 자라 지구를 지키는 큰 힘이 되는 날.",
                        "https://storage.googleapis.com/leafresh-images/init/4_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.jpg",
                        "https://storage.googleapis.com/leafresh-images/init/4_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/init/4_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png",
                        4, 5, 7
                ),

                new ChallengeSeed(
                        "지구야, 미안하고 고마워 \uD83C\uDF0D 편지 쓰기 챌린지",
                        "4월 22일, 지구의 날을 맞아 우리의 소중한 행성 ‘지구’에게 진심을 담아 편지를 써보세요.\n" +
                                "무심코 지나친 쓰레기, 무분별한 소비에 대한 미안함, 그리고 늘 우리를 품어주는 자연에 대한 고마움을 글로 남겨보는 시간입니다.\n" +
                                "손편지든 메모든 좋아요. 따뜻한 말 한마디가 행동으로 이어지는 시작이 될 수 있습니다.\n" +
                                "SNS에 공유하고, Leafresh 챌린지 인증 페이지에 올려 함께 나눠요 \uD83C\uDF31",
                        "https://storage.googleapis.com/leafresh-images/init/5_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.jpg",
                        "https://storage.googleapis.com/leafresh-images/init/5_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/init/5_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png",
                        4, 22, 7
                ),

                new ChallengeSeed(
                        "음식물도 순환돼요! 퇴비 챌린지",
                        "5월 첫째 주는 ‘세계 퇴비주간’입니다.\n" +
                                "우리가 매일 남기는 음식물 쓰레기, 사실 그 절반 이상은 다시 자연으로 되돌아갈 수 있다는 사실, 알고 계셨나요?\n" +
                                "이번 챌린지에서는 ‘밥 한 톨도 남기지 않기’, 즉 남기지 않고 먹기 실천을 중심으로 퇴비의 의미를 함께 알아보는 활동을 진행합니다.\n" +
                                "음식물 쓰레기를 줄이는 가장 쉬운 방법은 남기지 않는 것!\n" +
                                "작은 실천이지만, 생명을 살리는 자원순환의 시작입니다.",
                        "https://storage.googleapis.com/leafresh-images/init/6_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.jpg",
                        "https://storage.googleapis.com/leafresh-images/init/6_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/init/6_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png",
                        5, 1, 7
                ),

                new ChallengeSeed(
                        "착한 소비, 지구도 사람도 웃게 해요",
                        "5월 14일은 공정무역의 날입니다.\n" +
                                "우리가 마시는 커피 한 잔, 먹는 초콜릿 하나가 누군가의 착취 위에서 만들어졌을 수 있다는 사실, 알고 계셨나요?\n" +
                                "오늘 하루는 노동자의 정당한 대가를 보장하는 ‘공정무역 제품’을 선택해보세요.\n" +
                                "작은 소비가 지구와 사람을 살리는 변화가 됩니다.\n" +
                                "공정무역 인증 마크가 있는 제품을 구매 후 인증해주세요. 혹은 가까운 공정무역 가게/브랜드 소개도 좋습니다.",
                        "https://storage.googleapis.com/leafresh-images/init/7_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/init/7_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/init/7_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png",
                        5, 14, 7
                ),

                new ChallengeSeed(
                        "오늘은 바다를 위해 한 걸음",
                        "5월 31일 바다의 날을 맞아,\n" +
                                "바다를 쓰레기 처리장이 아닌 생명 가득한 생태계로 되돌리기 위한 실천을 함께해요.\n" +
                                "바다를 지키는 건 멀리 있는 일이 아니라, 오늘 내가 줄인 플라스틱, 오늘 내가 주운 쓰레기 한 조각에서 시작됩니다.\n" +
                                "가까운 해안, 하천, 공원 등에서 바다로 흘러갈 수 있는 쓰레기를 줍거나, 일회용품 사용을 줄여 바다의 하루를 지켜주세요 \uD83C\uDF0A",
                        "https://storage.googleapis.com/leafresh-images/init/8_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/init/8_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/init/8_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png",
                        5, 31, 7
                ),

                new ChallengeSeed(
                        "나의 환경 한 가지 실천 DAY",
                        "6월 5일은 환경의 날입니다.\n" +
                                "기후변화, 생물 다양성 파괴, 쓰레기 문제, 플라스틱 오염 등 우리가 직면한 환경 문제는 많지만, 그 출발점은 나의 하루, 나의 습관에서 시작됩니다.\n" +
                                "오늘 하루, '안쓰는 플러그 뽑기'에 다 같이 동참해주세요.\n" +
                                "작은 변화가 큰 울림이 될 수 있습니다 \uD83C\uDF31",
                        "https://storage.googleapis.com/leafresh-images/init/9_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.jpg",
                        "https://storage.googleapis.com/leafresh-images/init/9_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/init/9_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png",
                        6, 5, 7
                ),

                new ChallengeSeed(
                        "양치컵 하나로 지구를 살려요!",
                        "양치할 때 물을 틀어놓으면 약 6L의 물이 그냥 흘러가 버린다는 사실, 알고 계셨나요?\n" +
                                "6월 17일 사막화와 가뭄 방지의 날을 맞아, 오늘 하루 양치할 때 컵을 사용하는 실천에 동참해 주세요.\n" +
                                "양치컵 하나로 하루 5L 이상 절약할 수 있고,\n" +
                                "이건 한 사람이 마실 수 있는 ‘하루치 식수’와도 맞먹는 양이에요.\n" +
                                "습관 하나가 지구의 물을 살립니다 \uD83D\uDCA7\uD83C\uDF0D",
                        "https://storage.googleapis.com/leafresh-images/init/10_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.jpg",
                        "https://storage.googleapis.com/leafresh-images/init/10_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/init/10_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png",
                        6, 17, 7
                ),

                new ChallengeSeed(
                        "호랑이를 지켜요! 숲을 위한 하루",
                        "7월 30일은 세계 호랑이의 날입니다.\n" +
                                "숲이 파괴되면 호랑이는 사라지고, 호랑이가 사라지면 생태계의 균형도 무너집니다.\n" +
                                "오늘 하루는 ‘호랑이와 숲을 지키는 실천’을 함께 해보는 날입니다.\n" +
                                "종이 사용 줄이기, 플라스틱 줄이기, 채식 식단, 숲 걷기 등\n" +
                                "내가 숲을 아끼는 행동 하나가 멸종 위기 생물을 살리는 길입니다.",
                        "https://storage.googleapis.com/leafresh-images/init/11_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.jpg",
                        "https://storage.googleapis.com/leafresh-images/init/11_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/init/11_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png",
                        7, 30, 7
                ),

                new ChallengeSeed(
                        "꺼주세요 1시간! 에너지를 아끼는 시간 OFF",
                        "8월 22일 에너지의 날에는 저녁 9시부터 10시까지 1시간 동안 불을 끄는 캠페인이 전국적으로 진행됩니다.\n" +
                                "우리도 Leafresh에서 함께 실천해볼까요?\n" +
                                "하루 1시간, 조명, 전자기기, 에어컨, 멀티탭 전원을 OFF 해보며\n" +
                                "에너지를 아끼고 지구를 식히는 조용한 연대를 경험해보세요.\n" +
                                "에너지 절약은 곧 탄소 절감입니다.",
                        "https://storage.googleapis.com/leafresh-images/init/12_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.jpg",
                        "https://storage.googleapis.com/leafresh-images/init/12_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/init/12_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png",
                        8, 22, 7
                ),

                new ChallengeSeed(
                        "버리지 마세요! 오늘은 자원순환 챌린지",
                        "9월 6일은 자원순환의 날입니다.\n" +
                                "자원의 90% 이상을 수입에 의존하는 우리나라는 ‘버리기’보다는 ‘다시 쓰기’가 생존과 직결된 문제예요.\n" +
                                "오늘 하루, 하나라도 재사용하거나, 바르게 분리배출하거나, 새 쓰레기를 만들지 않는 행동을 실천해보세요.\n" +
                                "작은 선택이 자원을 살리고 지구를 살립니다 ♻\uFE0F",
                        "https://storage.googleapis.com/leafresh-images/init/13_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.jpg",
                        "https://storage.googleapis.com/leafresh-images/init/13_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/init/13_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png",
                        9, 6, 7
                ),

                new ChallengeSeed(
                        "오늘은 걷거나 타세요! Car-Free 실천 챌린지",
                        "9월 22일은 ‘세계 차 없는 날’입니다.\n" +
                                "오늘 하루, 나의 출퇴근길·등하굣길·약속길을 자동차 대신 도보, 자전거, 대중교통으로 실천해보세요.\n" +
                                "엔진 대신 두 발로 움직이는 하루는 탄소를 줄이고, 공기를 맑게 하고, 건강까지 지켜줘요.\n" +
                                "지구가 조용히 웃는 하루, 우리도 함께 걸어볼까요? \uD83C\uDF0E\uD83D\uDEB6\u200D♀\uFE0F",
                        "https://storage.googleapis.com/leafresh-images/init/14_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/init/14_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/init/14_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png",
                        9, 22, 7
                ),

                new ChallengeSeed(
                        "그날, 지구는 아팠어요 – 기후재난 이야기 공유 챌린지",
                        "10월 13일은 세계 자연재해 감소의 날입니다.\n" +
                                "우리가 겪고 있는 홍수, 폭염, 산불, 가뭄은 이제 자연이 아닌 ‘기후 재난’입니다.\n" +
                                "오늘은 지구에서 일어난 기후 재난에 관한 기사나 영상 하나를 읽고/보고,\n" +
                                "그 내용에 대한 나의 생각, 감정, 다짐을 짧게 기록해보는 챌린지예요.\n" +
                                "기록 후, 인증을 통해 다 같이 나누어보아요.\n" +
                                "공감은 행동의 시작입니다.",
                        "https://storage.googleapis.com/leafresh-images/init/15_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.jpg",
                        "https://storage.googleapis.com/leafresh-images/init/15_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/init/15_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png",
                        10, 13, 7
                ),

                new ChallengeSeed(
                        "오늘은 비건 한 끼, 지구와 나를 위한 식사",
                        "10월 16일은 세계 식량의 날입니다.\n" +
                                "지금도 9명 중 1명이 굶주리고, 또 다른 누군가는 먹을 만큼을 버립니다.\n" +
                                "지구의 자원을 지키고, 모두가 함께 먹기 위해 오늘 하루 비건 식사 한 끼를 실천해보세요.\n" +
                                "고기 대신 채소, 인스턴트 대신 자연식, 화학조미료 대신 건강한 조리로\n" +
                                "지구도 나도 건강해지는 식사를 경험해봐요 \uD83C\uDF3E\uD83E\uDD57",
                        "https://storage.googleapis.com/leafresh-images/init/16_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AFjpg.jpg",
                        "https://storage.googleapis.com/leafresh-images/init/16_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/init/16_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png",
                        10, 16, 7
                ),

                new ChallengeSeed(
                        "한 뼘의 텃밭, 농민의 마음을 심어요",
                        "11월 11일 농민의 날을 맞아,\n" +
                                "오늘은 집에서 작지만 소중한 먹거리 한 포기를 직접 심어보는 날입니다.\n" +
                                "방울토마토, 상추, 허브처럼 베란다나 창가에서도 키울 수 있는 작물부터 시작해보세요.\n" +
                                "흙을 만지고 물을 주며, 우리가 매일 먹는 것들의 시작이 얼마나 귀한지 직접 느껴보는 경험이 될 거예요 \uD83C\uDF3F\uD83C\uDF45",
                        "https://storage.googleapis.com/leafresh-images/init/17_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/init/17_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/init/17_%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png",
                        11, 11, 7
                )
        );
    }

    private record ChallengeSeed(
            String title,
            String description,
            String thumbnail,
            String success,
            String fail,
            int startMonth,
            int startDay,
            int durationDays
    ) {}
}
