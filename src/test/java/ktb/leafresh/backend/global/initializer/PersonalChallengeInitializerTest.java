package ktb.leafresh.backend.global.initializer;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallengeExampleImage;
import ktb.leafresh.backend.domain.challenge.personal.infrastructure.repository.PersonalChallengeExampleImageRepository;
import ktb.leafresh.backend.domain.challenge.personal.infrastructure.repository.PersonalChallengeRepository;
import ktb.leafresh.backend.global.common.entity.enums.ExampleImageType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PersonalChallengeInitializer 테스트")
class PersonalChallengeInitializerTest {

    @Mock
    private PersonalChallengeRepository challengeRepository;

    @Mock
    private PersonalChallengeExampleImageRepository imageRepository;

    @InjectMocks
    private PersonalChallengeInitializer initializer;

    @Test
    @DisplayName("이미 개인 챌린지가 존재하면 초기화되지 않는다")
    void run_whenPersonalChallengesExist_thenDoNothing() throws Exception {
        // given
        given(challengeRepository.count()).willReturn(5L);

        // when
        initializer.run();

        // then
        then(challengeRepository).should(never()).save(any());
        then(imageRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("개인 챌린지가 존재하지 않으면 저장된다")
    void run_whenNoChallenges_thenSavesAll() throws Exception {
        // given
        given(challengeRepository.count()).willReturn(0L);
        given(challengeRepository.save(any(PersonalChallenge.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        initializer.run();

        // then
        then(imageRepository).should(atLeastOnce()).saveAll(argThat(images -> {
            List<PersonalChallengeExampleImage> list = StreamSupport
                    .stream(images.spliterator(), false)
                    .collect(Collectors.toList());

            return list.size() == 2 &&
                    list.get(0).getType() == ExampleImageType.SUCCESS &&
                    list.get(1).getType() == ExampleImageType.FAILURE;
        }));
    }
}
