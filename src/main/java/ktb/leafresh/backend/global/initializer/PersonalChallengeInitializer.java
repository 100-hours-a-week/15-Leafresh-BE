package ktb.leafresh.backend.global.initializer;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.challenge.personal.infrastructure.repository.PersonalChallengeExampleImageRepository;
import ktb.leafresh.backend.domain.challenge.personal.infrastructure.repository.PersonalChallengeRepository;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PersonalChallengeInitializer implements CommandLineRunner {

    private final PersonalChallengeRepository challengeRepository;
    private final PersonalChallengeExampleImageRepository imageRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (challengeRepository.count() > 0) return;

        DayOfWeek[] days = DayOfWeek.values();

        List<ChallengeSeed> seeds = List.of(
                new ChallengeSeed("텀블러 사용하기", "일회용 컵 대신 텀블러를 사용해 환경 보호에 동참해요.",
                        "https://storage.googleapis.com/leafresh-images/images/1_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/images/1_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/images/1_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png"),

                new ChallengeSeed("에코백 사용하기", "장보거나 외출할 때 비닐 대신 에코백을 사용해보세요.",
                        "https://storage.googleapis.com/leafresh-images/images/2_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/images/2_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.jpg",
                        "https://storage.googleapis.com/leafresh-images/images/2_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png"),

                new ChallengeSeed("장바구니 사용하기", "마트나 편의점에서 장바구니를 챙겨 비닐 사용을 줄여보세요.",
                        "https://storage.googleapis.com/leafresh-images/images/3_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/images/3_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.jpg",
                        "https://storage.googleapis.com/leafresh-images/images/3_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png"),

                new ChallengeSeed("자전거 타기", "자동차 대신 자전거로 이동해 탄소 배출을 줄여보세요.",
                        "https://storage.googleapis.com/leafresh-images/images/4_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/images/4_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.jpg",
                        "https://storage.googleapis.com/leafresh-images/images/4_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png"),

                new ChallengeSeed("대중교통 이용하기", "자가용 대신 버스나 지하철을 이용해 환경 보호에 참여해요.",
                        "https://storage.googleapis.com/leafresh-images/images/5_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/images/5_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.jpg",
                        "https://storage.googleapis.com/leafresh-images/images/5_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png"),

                new ChallengeSeed("샐러드/채식 식단 먹기", "하루 한 끼 채식으로 지구 온난화에 대응해요.",
                        "https://storage.googleapis.com/leafresh-images/images/6_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/images/6_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.jpg",
                        "https://storage.googleapis.com/leafresh-images/images/6_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png"),

                new ChallengeSeed("음식 남기지 않기", "한 그릇 깨끗이 비우며 음식물 쓰레기를 줄여보세요.",
                        "https://storage.googleapis.com/leafresh-images/images/7_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/images/7_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/images/7_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png"),

                new ChallengeSeed("계단 이용하기", "엘리베이터 대신 계단을 이용해 에너지를 절약해요.",
                        "https://storage.googleapis.com/leafresh-images/images/8_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/images/8_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.jpg",
                        "https://storage.googleapis.com/leafresh-images/images/8_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png"),

                new ChallengeSeed("재활용 분리수거하기", "종이·플라스틱을 정확하게 분리수거해요.",
                        "https://storage.googleapis.com/leafresh-images/images/9_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/images/9_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/images/9_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png"),

                new ChallengeSeed("손수건 사용하기", "휴지 대신 손수건을 사용해 일회용품 사용을 줄여보세요.",
                        "https://storage.googleapis.com/leafresh-images/images/9_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png",
                        "https://storage.googleapis.com/leafresh-images/images/10_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/images/10_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png"),

                new ChallengeSeed("쓰레기 줍기", "길거리의 쓰레기를 직접 주워 깨끗한 환경을 만들어요.",
                        "https://storage.googleapis.com/leafresh-images/images/11_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/images/11_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.jpg",
                        "https://storage.googleapis.com/leafresh-images/images/11_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png"),

                new ChallengeSeed("안 쓰는 전기 플러그 뽑기", "사용하지 않는 플러그는 빼두어 전력 낭비를 줄여요.",
                        "https://storage.googleapis.com/leafresh-images/images/12_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/images/12_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/images/12_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png"),

                new ChallengeSeed("고체 비누 사용하기", "플라스틱 용기 없는 고체 비누로 욕실을 바꿔보세요.",
                        "https://storage.googleapis.com/leafresh-images/images/13_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/images/13_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.jpg",
                        "https://storage.googleapis.com/leafresh-images/images/13_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png"),

                new ChallengeSeed("하루 만 보 걷기", "하루 만 보 걷기로 건강과 환경을 모두 챙겨요.",
                        "https://storage.googleapis.com/leafresh-images/images/14_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/images/14_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/images/14_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.jpg"),

                new ChallengeSeed("도시락 싸먹기", "일회용 포장 대신 도시락으로 제로 웨이스트 실천해요.",
                        "https://storage.googleapis.com/leafresh-images/images/15_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/images/15_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.jpg",
                        "https://storage.googleapis.com/leafresh-images/images/15_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png"),

                new ChallengeSeed("작은 텃밭 가꾸기", "집에서 기른 채소로 환경과 건강을 함께 챙겨요.",
                        "https://storage.googleapis.com/leafresh-images/images/16_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/images/16_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.jpg",
                        "https://storage.googleapis.com/leafresh-images/images/16_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png"),

                new ChallengeSeed("반려 식물 인증", "하루 한 번 반려 식물을 돌보며 초록 일상을 실천해요.",
                        "https://storage.googleapis.com/leafresh-images/images/17_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/images/17_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/images/17_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png"),

                new ChallengeSeed("전자 영수증 받기", "종이 영수증 대신 전자 영수증을 요청해봐요.",
                        "https://storage.googleapis.com/leafresh-images/images/18_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.jpg",
                        "https://storage.googleapis.com/leafresh-images/images/18_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.jpg",
                        "https://storage.googleapis.com/leafresh-images/images/18_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png"),

                new ChallengeSeed("친환경 인증 마크 상품 구매하기", "FSC·비건 마크 등 인증 제품을 구매해요.",
                        "https://storage.googleapis.com/leafresh-images/images/19_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/images/19_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/images/19_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png"),

                new ChallengeSeed("다회용기 사용하기", "음식 포장 시 다회용기를 사용해 일회용품을 줄여요.",
                        "https://storage.googleapis.com/leafresh-images/images/20_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.jpg",
                        "https://storage.googleapis.com/leafresh-images/images/20_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.jpg",
                        "https://storage.googleapis.com/leafresh-images/images/20_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png"),

                new ChallengeSeed("책·전자책 읽기 인증하기", "환경 관련 책이나 전자책을 읽고 인증해요.",
                        "https://storage.googleapis.com/leafresh-images/images/21_%E1%84%8A%E1%85%A5%E1%86%B7%E1%84%82%E1%85%A6%E1%84%8B%E1%85%B5%E1%86%AF.png",
                        "https://storage.googleapis.com/leafresh-images/images/21_%E1%84%89%E1%85%A5%E1%86%BC%E1%84%80%E1%85%A9%E1%86%BC.png",
                        "https://storage.googleapis.com/leafresh-images/images/21_%E1%84%89%E1%85%B5%E1%86%AF%E1%84%91%E1%85%A2.png")
        );

        for (int i = 0; i < seeds.size(); i++) {
            ChallengeSeed seed = seeds.get(i);
            DayOfWeek day = days[i / 3];

            PersonalChallenge challenge = challengeRepository.save(
                    PersonalChallenge.builder()
                            .title(seed.title())
                            .description(seed.description())
                            .imageUrl(seed.thumbnail())
                            .leafReward(30)
                            .dayOfWeek(day)
                            .verificationStartTime(LocalTime.of(6, 0))
                            .verificationEndTime(LocalTime.of(23, 0))
                            .build()
            );

            imageRepository.saveAll(List.of(
                    PersonalChallenge.of(challenge, seed.success(), ExampleImageType.SUCCESS, "성공 예시입니다.", 1),
                    PersonalChallenge.of(challenge, seed.fail(), ExampleImageType.FAILURE, "실패 예시입니다.", 2)
            ));
        }
    }

    private record ChallengeSeed(String title, String description, String thumbnail, String success, String fail) {}
}
