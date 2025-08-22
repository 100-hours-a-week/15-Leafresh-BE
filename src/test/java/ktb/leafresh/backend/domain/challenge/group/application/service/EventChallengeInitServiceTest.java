package ktb.leafresh.backend.domain.challenge.group.application.service;

import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallenge;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.GroupChallengeCategory;
import ktb.leafresh.backend.domain.challenge.group.domain.entity.enums.GroupChallengeCategoryName;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeCategoryRepository;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeExampleImageRepository;
import ktb.leafresh.backend.domain.challenge.group.infrastructure.repository.GroupChallengeRepository;
import ktb.leafresh.backend.domain.member.domain.entity.Member;
import ktb.leafresh.backend.domain.member.domain.entity.TreeLevel;
import ktb.leafresh.backend.domain.member.infrastructure.repository.MemberRepository;
import ktb.leafresh.backend.domain.member.infrastructure.repository.TreeLevelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventChallengeInitService 테스트")
public class EventChallengeInitServiceTest {

  @Mock private GroupChallengeRepository challengeRepository;

  @Mock private GroupChallengeCategoryRepository categoryRepository;

  @Mock private GroupChallengeExampleImageRepository imageRepository;

  @Mock private MemberRepository memberRepository;

  @Mock private TreeLevelRepository treeLevelRepository;

  @InjectMocks private EventChallengeInitService eventChallengeInitService;

  @Test
  @DisplayName("이벤트 챌린지가 이미 존재하면 등록하지 않는다")
  void registerEventChallenge_ifAlreadyExists_doesNothing() {
    // given
    int year = LocalDate.now().getYear();
    String title = "SNS에 습지 보호 캠페인 알리기 " + year;
    given(challengeRepository.existsByTitleAndEventFlagTrue(title)).willReturn(true);

    // when
    eventChallengeInitService.registerCurrentYearEventChallengesIfNotExists();

    // then
    verify(challengeRepository, never()).save(any());
    verify(imageRepository, never()).saveAll(any());
  }

  @Test
  @DisplayName("ETC 카테고리가 없으면 예외 발생")
  void registerEventChallenge_ifCategoryNotFound_throwsException() {
    // given
    int year = LocalDate.now().getYear();
    given(challengeRepository.existsByTitleAndEventFlagTrue(anyString())).willReturn(false);
    given(categoryRepository.findByName(GroupChallengeCategoryName.ETC.name()))
        .willReturn(Optional.empty());

    // when/then
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalStateException.class,
        () -> eventChallengeInitService.registerCurrentYearEventChallengesIfNotExists());
  }

  @Test
  @DisplayName("admin 계정이 없으면 새로 생성한다")
  void registerEventChallenge_ifAdminNotExist_createAdminMember() {
    // given
    int year = LocalDate.now().getYear();
    GroupChallengeCategory etc = mock(GroupChallengeCategory.class);
    TreeLevel treeLevel = mock(TreeLevel.class);

    given(challengeRepository.existsByTitleAndEventFlagTrue(anyString())).willReturn(false);
    given(categoryRepository.findByName(GroupChallengeCategoryName.ETC.name()))
        .willReturn(Optional.of(etc));
    given(memberRepository.findByEmail("admin@leafresh.io")).willReturn(Optional.empty());
    given(treeLevelRepository.findById(1L)).willReturn(Optional.of(treeLevel));
    given(memberRepository.save(any(Member.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // when
    eventChallengeInitService.registerCurrentYearEventChallengesIfNotExists();

    // then
    verify(memberRepository).save(any(Member.class));
    verify(challengeRepository, atLeastOnce()).save(any(GroupChallenge.class));
    verify(imageRepository, atLeastOnce()).saveAll(any());
  }

  @Test
  @DisplayName("admin 계정이 있으면 재사용한다")
  void registerEventChallenge_ifAdminExists_useExistingAdmin() {
    // given
    int year = LocalDate.now().getYear();
    GroupChallengeCategory etc = mock(GroupChallengeCategory.class);
    Member admin = mock(Member.class);

    given(challengeRepository.existsByTitleAndEventFlagTrue(anyString())).willReturn(false);
    given(categoryRepository.findByName(GroupChallengeCategoryName.ETC.name()))
        .willReturn(Optional.of(etc));
    given(memberRepository.findByEmail("admin@leafresh.io")).willReturn(Optional.of(admin));

    // when
    eventChallengeInitService.registerCurrentYearEventChallengesIfNotExists();

    // then
    verify(memberRepository, never()).save(any());
    verify(challengeRepository, atLeastOnce()).save(any(GroupChallenge.class));
    verify(imageRepository, atLeastOnce()).saveAll(any());
  }
}
