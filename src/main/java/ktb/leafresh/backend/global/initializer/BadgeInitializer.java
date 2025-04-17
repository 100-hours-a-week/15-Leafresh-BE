package ktb.leafresh.backend.global.initializer;

import ktb.leafresh.backend.domain.member.domain.entity.Badge;
import ktb.leafresh.backend.domain.member.domain.entity.enums.BadgeType;
import ktb.leafresh.backend.domain.member.infrastructure.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BadgeInitializer implements CommandLineRunner {

    private final BadgeRepository badgeRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<BadgeSeed> badgeSeeds = List.of(
                // GROUP
                new BadgeSeed(BadgeType.GROUP, "제로 히어로", "제로웨이스트 챌린지 3개에 참여하고, 각 챌린지에서 한 번씩 인증에 성공해야 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/group/%E1%84%8C%E1%85%A6%E1%84%85%E1%85%A9%E1%84%8B%E1%85%B0%E1%84%8B%E1%85%B5%E1%84%89%E1%85%B3%E1%84%90%E1%85%B3.png"),
                new BadgeSeed(BadgeType.GROUP, "플로깅 파이터", "플로깅 챌린지 3개에 참여하고, 각 챌린지에서 한 번씩 인증에 성공해야 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/group/%E1%84%91%E1%85%B3%E1%86%AF%E1%84%85%E1%85%A9%E1%84%80%E1%85%B5%E1%86%BC.png"),
                new BadgeSeed(BadgeType.GROUP, "발자국 줄이기 고수", "탄소 발자국 챌린지 3개에 참여하고, 각 챌린지에서 한 번씩 인증에 성공해야 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/group/%E1%84%90%E1%85%A1%E1%86%AB%E1%84%89%E1%85%A9_%E1%84%87%E1%85%A1%E1%86%AF%E1%84%8C%E1%85%A1%E1%84%80%E1%85%AE%E1%86%A8.png"),
                new BadgeSeed(BadgeType.GROUP, "절전 마스터", "에너지 절약 챌린지 3개에 참여하고, 각 챌린지에서 한 번씩 인증에 성공해야 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/group/%E1%84%8B%E1%85%A6%E1%84%82%E1%85%A5%E1%84%8C%E1%85%B5_%E1%84%8C%E1%85%A5%E1%86%AF%E1%84%8B%E1%85%A3%E1%86%A8.png"),
                new BadgeSeed(BadgeType.GROUP, "새활용 장인", "업사이클 챌린지 3개에 참여하고, 각 챌린지에서 한 번씩 인증에 성공해야 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/group/%E1%84%8C%E1%85%AE%E1%86%BC%E1%84%80%E1%85%A9%E1%84%80%E1%85%A5%E1%84%85%E1%85%A2_%E1%84%8B%E1%85%A5%E1%86%B8%E1%84%89%E1%85%A1%E1%84%8B%E1%85%B5%E1%84%8F%E1%85%B3%E1%86%AF.png"),
                new BadgeSeed(BadgeType.GROUP, "녹색 지식인", "문화 공유 챌린지 3개에 참여하고, 각 챌린지에서 한 번씩 인증에 성공해야 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/group/%E1%84%86%E1%85%AE%E1%86%AB%E1%84%92%E1%85%AA_%E1%84%80%E1%85%A9%E1%86%BC%E1%84%8B%E1%85%B2.png"),
                new BadgeSeed(BadgeType.GROUP, "디지털 디톡서", "디지털 탄소 챌린지 3개에 참여하고, 각 챌린지에서 한 번씩 인증에 성공해야 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/group/%E1%84%83%E1%85%B5%E1%84%8C%E1%85%B5%E1%84%90%E1%85%A5%E1%86%AF_%E1%84%90%E1%85%A1%E1%86%AB%E1%84%89%E1%85%A9.png"),
                new BadgeSeed(BadgeType.GROUP, "비건 챌린저", "비건 챌린지 3개에 참여하고, 각 챌린지에서 한 번씩 인증에 성공해야 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/group/%E1%84%87%E1%85%B5%E1%84%80%E1%85%A5%E1%86%AB.png"),

                // PERSONAL
                new BadgeSeed(BadgeType.PERSONAL, "새싹 실천러", "개인 챌린지에서 3일 연속으로 인증에 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/personal/3%E1%84%8B%E1%85%B5%E1%86%AF_%E1%84%8B%E1%85%A7%E1%86%AB%E1%84%89%E1%85%A9%E1%86%A8.png"),
                new BadgeSeed(BadgeType.PERSONAL, "일주일의 습관", "개인 챌린지에서 7일 연속으로 인증에 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/personal/7%E1%84%8B%E1%85%B5%E1%86%AF_%E1%84%8B%E1%85%A7%E1%86%AB%E1%84%89%E1%85%A9%E1%86%A8.png"),
                new BadgeSeed(BadgeType.PERSONAL, "반달 에코러", "개인 챌린지에서 14일 연속으로 인증에 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/personal/14%E1%84%8B%E1%85%B5%E1%86%AF_%E1%84%8B%E1%85%A7%E1%86%AB%E1%84%89%E1%85%A9%E1%86%A8.png"),
                new BadgeSeed(BadgeType.PERSONAL, "한 달 챌린지 완주자", "개인 챌린지에서 30일 연속으로 인증에 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/personal/30%E1%84%8B%E1%85%B5%E1%86%AF_%E1%84%8B%E1%85%A7%E1%86%AB%E1%84%89%E1%85%A9%E1%86%A8.png"),

                // TOTAL
                new BadgeSeed(BadgeType.TOTAL, "첫 발자국", "개인 및 단체 챌린지 인증 성공 총 10회를 달성하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/total/%E1%84%82%E1%85%AE%E1%84%8C%E1%85%A5%E1%86%A8%E1%84%8E%E1%85%A2%E1%86%AF%E1%84%85%E1%85%B5%E1%86%AB%E1%84%8C%E1%85%B5_10.png"),
                new BadgeSeed(BadgeType.TOTAL, "실천 중급자", "개인 및 단체 챌린지 인증 성공 총 30회를 달성하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/total/%E1%84%82%E1%85%AE%E1%84%8C%E1%85%A5%E1%86%A8%E1%84%8E%E1%85%A2%E1%86%AF%E1%84%85%E1%85%B5%E1%86%AB%E1%84%8C%E1%85%B5_30.png"),
                new BadgeSeed(BadgeType.TOTAL, "지속가능 파이터", "개인 및 단체 챌린지 인증 성공 총 50회를 달성하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/total/%E1%84%82%E1%85%AE%E1%84%8C%E1%85%A5%E1%86%A8%E1%84%8E%E1%85%A2%E1%86%AF%E1%84%85%E1%85%B5%E1%86%AB%E1%84%8C%E1%85%B5_50.png"),
                new BadgeSeed(BadgeType.TOTAL, "그린 마스터", "개인 및 단체 챌린지 인증 성공 총 100회를 달성하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/total/%E1%84%82%E1%85%AE%E1%84%8C%E1%85%A5%E1%86%A8%E1%84%8E%E1%85%A2%E1%86%AF%E1%84%85%E1%85%B5%E1%86%AB%E1%84%8C%E1%85%B5_100.png"),

                // SPECIAL
                new BadgeSeed(BadgeType.SPECIAL, "지속가능 전도사", "모든 카테고리의 단체 챌린지에 각각 한 번 이상 참여해 인증에 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/special/%E1%84%89%E1%85%B3%E1%84%91%E1%85%A6%E1%84%89%E1%85%A7%E1%86%AF%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_1_%E1%84%8C%E1%85%B5%E1%84%89%E1%85%A9%E1%86%A8%E1%84%80%E1%85%A1%E1%84%82%E1%85%B3%E1%86%BC%E1%84%8C%E1%85%A5%E1%86%AB%E1%84%83%E1%85%A9%E1%84%89%E1%85%A1.png"),
                new BadgeSeed(BadgeType.SPECIAL, "도전 전부러", "모든 개인 챌린지에서 최소 한 번 이상 인증에 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/special/%E1%84%89%E1%85%B3%E1%84%91%E1%85%A6%E1%84%89%E1%85%A7%E1%86%AF%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_2_%E1%84%83%E1%85%A9%E1%84%8C%E1%85%A5%E1%86%AB%E1%84%8C%E1%85%A5%E1%86%AB%E1%84%87%E1%85%AE%E1%84%85%E1%85%A5.png"),
                new BadgeSeed(BadgeType.SPECIAL, "마스터", "하나의 단체 챌린지 카테고리에서 10회 이상 인증 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/special/%E1%84%89%E1%85%B3%E1%84%91%E1%85%A6%E1%84%89%E1%85%A7%E1%86%AF%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_3_%E1%84%86%E1%85%A1%E1%84%89%E1%85%B3%E1%84%90%E1%85%A5.png"),
                new BadgeSeed(BadgeType.SPECIAL, "에코 슈퍼루키", "개인 챌린지를 30일 연속으로 인증에 성공하면 숨겨진 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/special/%E1%84%89%E1%85%B3%E1%84%91%E1%85%A6%E1%84%89%E1%85%A7%E1%86%AF%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_4_%E1%84%8B%E1%85%A6%E1%84%8F%E1%85%A9%E1%84%89%E1%85%B2%E1%84%91%E1%85%A5%E1%84%85%E1%85%AE%E1%84%8F%E1%85%B5.png"),

                // EVENT
                new BadgeSeed(BadgeType.EVENT, "습지 전도사", "'세계 습지의 날' 이벤트 챌린지에서 인증을 3회 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/event/%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_1_%E1%84%89%E1%85%A6%E1%84%80%E1%85%A8%E1%84%89%E1%85%B3%E1%86%B8%E1%84%8C%E1%85%B5%E1%84%8B%E1%85%B4%E1%84%82%E1%85%A1%E1%86%AF.png"),
                new BadgeSeed(BadgeType.EVENT, "바다 지킴이", "'고래의 날' 이벤트 챌린지에서 인증을 3회 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/event/%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_2_%E1%84%80%E1%85%A9%E1%84%85%E1%85%A2%E1%84%8B%E1%85%B4%E1%84%82%E1%85%A1%E1%86%AF.png"),
                new BadgeSeed(BadgeType.EVENT, "물수호대", "'세계 물의 날' 이벤트 챌린지에서 인증을 3회 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/event/%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_3_%E1%84%89%E1%85%A6%E1%84%80%E1%85%A8%E1%84%86%E1%85%AE%E1%86%AF%E1%84%8B%E1%85%B4%E1%84%82%E1%85%A1%E1%86%AF.png"),
                new BadgeSeed(BadgeType.EVENT, "나무 한 그루의 기적", "'식목일' 이벤트 챌린지에서 인증을 3회 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/event/%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_4_%E1%84%89%E1%85%B5%E1%86%A8%E1%84%86%E1%85%A9%E1%86%A8%E1%84%8B%E1%85%B5%E1%86%AF.png"),
                new BadgeSeed(BadgeType.EVENT, "지구에게 쓴 편지", "'지구의 날' 이벤트 챌린지에서 인증을 3회 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/event/%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_5_%E1%84%8C%E1%85%B5%E1%84%80%E1%85%AE%E1%84%8B%E1%85%B4%E1%84%82%E1%85%A1%E1%86%AF.png"),
                new BadgeSeed(BadgeType.EVENT, "퇴비 마스터", "'세계 퇴비 주간' 이벤트 챌린지에서 인증을 3회 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/event/%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_6_%E1%84%89%E1%85%A6%E1%84%80%E1%85%A8%E1%84%90%E1%85%AC%E1%84%87%E1%85%B5%E1%84%8C%E1%85%AE%E1%84%80%E1%85%A1%E1%86%AB.png"),
                new BadgeSeed(BadgeType.EVENT, "착한 소비러", "'공정무역의 날' 이벤트 챌린지에서 인증을 3회 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/event/%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_7_%E1%84%80%E1%85%A9%E1%86%BC%E1%84%8C%E1%85%A5%E1%86%BC%E1%84%86%E1%85%AE%E1%84%8B%E1%85%A7%E1%86%A8%E1%84%8B%E1%85%B4%E1%84%82%E1%85%A1%E1%86%AF.png"),
                new BadgeSeed(BadgeType.EVENT, "파도 위 발자국", "'바다의 날' 이벤트 챌린지에서 인증을 3회 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/event/%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_8_%E1%84%87%E1%85%A1%E1%84%83%E1%85%A1%E1%84%8B%E1%85%B4%E1%84%82%E1%85%A1%E1%86%AF.png"),
                new BadgeSeed(BadgeType.EVENT, "환경 루틴러", "'환경의 날' 이벤트 챌린지에서 인증을 3회 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/event/%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_9_%E1%84%89%E1%85%A6%E1%84%80%E1%85%A8%E1%84%92%E1%85%AA%E1%86%AB%E1%84%80%E1%85%A7%E1%86%BC%E1%84%8B%E1%85%B4%E1%84%82%E1%85%A1%E1%86%AF.png"),
                new BadgeSeed(BadgeType.EVENT, "양치컵 히어로", "'사막화와 가뭄 방지의 날' 이벤트 챌린지에서 인증을 3회 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/event/%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_10_%E1%84%89%E1%85%A1%E1%84%86%E1%85%A1%E1%86%A8%E1%84%92%E1%85%AA%E1%84%8B%E1%85%AA%E1%84%80%E1%85%A1%E1%84%86%E1%85%AE%E1%86%B7%E1%84%87%E1%85%A1%E1%86%BC%E1%84%8C%E1%85%B5%E1%84%8B%E1%85%B4%E1%84%82%E1%85%A1%E1%86%AF.png"),
                new BadgeSeed(BadgeType.EVENT, "호랑이 친구", "'세계 호랑이의 날' 이벤트 챌린지에서 인증을 3회 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/event/%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_11_%E1%84%80%E1%85%AE%E1%86%A8%E1%84%8C%E1%85%A6%E1%84%92%E1%85%A9%E1%84%85%E1%85%A1%E1%86%BC%E1%84%8B%E1%85%B5%E1%84%8B%E1%85%B4%E1%84%82%E1%85%A1%E1%86%AF.png"),
                new BadgeSeed(BadgeType.EVENT, "OFF 마스터", "'에너지의 날' 이벤트 챌린지에서 인증을 3회 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/event/%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_12_%E1%84%8B%E1%85%A6%E1%84%82%E1%85%A5%E1%84%8C%E1%85%B5%E1%84%8B%E1%85%B4%E1%84%82%E1%85%A1%E1%86%AF.png"),
                new BadgeSeed(BadgeType.EVENT, "자원순환 챔피언", "'자원순환의 날' 이벤트 챌린지에서 인증을 3회 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/event/%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_13_%E1%84%8C%E1%85%A1%E1%84%8B%E1%85%AF%E1%86%AB%E1%84%89%E1%85%AE%E1%86%AB%E1%84%92%E1%85%AA%E1%86%AB%E1%84%8B%E1%85%B4%E1%84%82%E1%85%A1%E1%86%AF.png"),
                new BadgeSeed(BadgeType.EVENT, "무탄소 여행자", "'세계 차 없는 날' 이벤트 챌린지에서 인증을 3회 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/event/%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_14_%E1%84%89%E1%85%A6%E1%84%80%E1%85%A8%E1%84%8E%E1%85%A1%E1%84%8B%E1%85%A5%E1%86%B9%E1%84%82%E1%85%B3%E1%86%AB%E1%84%82%E1%85%A1%E1%86%AF.png"),
                new BadgeSeed(BadgeType.EVENT, "기후 기록자", "'세계 자연재해 감소의 날' 이벤트 챌린지에서 인증을 3회 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/event/%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_15_%E1%84%89%E1%85%A6%E1%84%80%E1%85%A8%E1%84%8C%E1%85%A1%E1%84%8B%E1%85%A7%E1%86%AB%E1%84%8C%E1%85%A2%E1%84%92%E1%85%A2%E1%84%80%E1%85%A1%E1%86%B7%E1%84%89%E1%85%A9%E1%84%8B%E1%85%B4%E1%84%82%E1%85%A1%E1%86%AF.png"),
                new BadgeSeed(BadgeType.EVENT, "비건 한 끼 도전자", "'세계 식량의 날' 이벤트 챌린지에서 인증을 3회 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/event/%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_16_%E1%84%89%E1%85%A6%E1%84%80%E1%85%A8%E1%84%89%E1%85%B5%E1%86%A8%E1%84%85%E1%85%A3%E1%86%BC%E1%84%8B%E1%85%B4%E1%84%82%E1%85%A1%E1%86%AF.png"),
                new BadgeSeed(BadgeType.EVENT, "도시 농부", "'농민의 날' 이벤트 챌린지에서 인증을 3회 성공하면 뱃지를 획득할 수 있어요!", "https://storage.googleapis.com/leafresh-images/init/badge/event/%E1%84%8B%E1%85%B5%E1%84%87%E1%85%A6%E1%86%AB%E1%84%90%E1%85%B3%E1%84%87%E1%85%A2%E1%86%BA%E1%84%8C%E1%85%B5_17_%E1%84%89%E1%85%A6%E1%84%80%E1%85%A8%E1%84%82%E1%85%A9%E1%86%BC%E1%84%86%E1%85%B5%E1%86%AB%E1%84%8B%E1%85%B4%E1%84%82%E1%85%A1%E1%86%AF.png")


        );

        for (BadgeSeed seed : badgeSeeds) {
            if (badgeRepository.findByName(seed.name()).isEmpty()) {
                badgeRepository.save(
                        Badge.builder()
                                .type(seed.type())
                                .name(seed.name())
                                .condition(seed.condition())
                                .imageUrl(seed.imageUrl())
                                .build()
                );
            }
        }
    }

    private record BadgeSeed(BadgeType type, String name, String condition, String imageUrl) {}
}
