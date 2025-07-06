package ktb.leafresh.backend.global.initializer;

import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.domain.entity.enums.TreeLevelName;
import ktb.leafresh.backend.domain.member.infrastructure.repository.TreeLevelRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TreeLevelInitializer 테스트")
class TreeLevelInitializerTest {

    @Mock
    private TreeLevelRepository treeLevelRepository;

    @InjectMocks
    private TreeLevelInitializer initializer;

    @Test
    @DisplayName("모든 TreeLevel이 저장되어 있지 않으면 저장 로직이 실행된다")
    void run_whenAllLevelsNotExist_thenSaveEach() {
        // given
        for (TreeLevelName name : TreeLevelName.values()) {
            given(treeLevelRepository.findByName(name)).willReturn(Optional.empty());
        }

        // when
        initializer.run();

        // then
        for (TreeLevelName name : TreeLevelName.values()) {
            then(treeLevelRepository).should().save(
                    argThat(treeLevel ->
                            treeLevel.getName() == name &&
                                    treeLevel.getMinLeafPoint() >= 0 &&
                                    treeLevel.getImageUrl().contains(name.name()) &&
                                    treeLevel.getDescription().contains("단계")
                    )
            );
        }
    }

    @Test
    @DisplayName("일부 TreeLevel만 저장되어 있는 경우 나머지만 저장된다")
    void run_whenSomeLevelsExist_thenSaveMissingOnly() {
        // given
        given(treeLevelRepository.findByName(TreeLevelName.SPROUT)).willReturn(Optional.of(mock(TreeLevel.class)));
        given(treeLevelRepository.findByName(TreeLevelName.YOUNG)).willReturn(Optional.empty());
        given(treeLevelRepository.findByName(TreeLevelName.SMALL_TREE)).willReturn(Optional.empty());
        given(treeLevelRepository.findByName(TreeLevelName.TREE)).willReturn(Optional.of(mock(TreeLevel.class)));
        given(treeLevelRepository.findByName(TreeLevelName.BIG_TREE)).willReturn(Optional.empty());

        // when
        initializer.run();

        // then
        then(treeLevelRepository).should(never()).save(argThat(treeLevel -> treeLevel.getName() == TreeLevelName.SPROUT));
        then(treeLevelRepository).should(never()).save(argThat(treeLevel -> treeLevel.getName() == TreeLevelName.TREE));

        then(treeLevelRepository).should().save(argThat(treeLevel -> treeLevel.getName() == TreeLevelName.YOUNG));
        then(treeLevelRepository).should().save(argThat(treeLevel -> treeLevel.getName() == TreeLevelName.SMALL_TREE));
        then(treeLevelRepository).should().save(argThat(treeLevel -> treeLevel.getName() == TreeLevelName.BIG_TREE));
    }

    @Test
    @DisplayName("모든 TreeLevel이 이미 저장되어 있는 경우 저장하지 않는다")
    void run_whenAllLevelsExist_thenDoNothing() {
        // given
        for (TreeLevelName name : TreeLevelName.values()) {
            given(treeLevelRepository.findByName(name)).willReturn(Optional.of(mock(TreeLevel.class)));
        }

        // when
        initializer.run();

        // then
        then(treeLevelRepository).should(never()).save(any());
    }
}
