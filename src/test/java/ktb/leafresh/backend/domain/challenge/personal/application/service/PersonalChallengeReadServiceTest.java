package ktb.leafresh.backend.domain.challenge.personal.application.service;

import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallenge;
import ktb.leafresh.backend.domain.challenge.personal.domain.entity.PersonalChallengeExampleImage;
import ktb.leafresh.backend.domain.challenge.personal.infrastructure.repository.PersonalChallengeRepository;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response.PersonalChallengeDetailResponseDto;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response.PersonalChallengeListResponseDto;
import ktb.leafresh.backend.domain.challenge.personal.presentation.dto.response.PersonalChallengeRuleResponseDto;
import ktb.leafresh.backend.domain.verification.infrastructure.repository.PersonalChallengeVerificationRepository;
import ktb.leafresh.backend.global.common.entity.enums.ChallengeStatus;
import ktb.leafresh.backend.global.common.entity.enums.DayOfWeek;
import ktb.leafresh.backend.global.exception.ChallengeErrorCode;
import ktb.leafresh.backend.global.exception.CustomException;
import ktb.leafresh.backend.support.fixture.MemberFixture;
import ktb.leafresh.backend.support.fixture.PersonalChallengeExampleImageFixture;
import ktb.leafresh.backend.support.fixture.PersonalChallengeFixture;
import ktb.leafresh.backend.support.fixture.PersonalChallengeVerificationFixture;

import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@DisplayName("PersonalChallengeReadService 단위 테스트")
class PersonalChallengeReadServiceTest {

    @Mock
    private PersonalChallengeRepository challengeRepository;

    @Mock
    private PersonalChallengeVerificationRepository verificationRepository;

    @InjectMocks
    private PersonalChallengeReadService readService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("요일 기준으로 개인 챌린지를 조회할 수 있다")
    @Test
    void getByDayOfWeek_withValidDay_returnsList() {
        // given
        PersonalChallenge challenge = PersonalChallengeFixture.of("매일 물 마시기");
        ReflectionTestUtils.setField(challenge, "id", 1L);
        given(challengeRepository.findAllByDayOfWeek(DayOfWeek.MONDAY)).willReturn(List.of(challenge));

        // when
        PersonalChallengeListResponseDto response = readService.getByDayOfWeek(DayOfWeek.MONDAY);

        // then
        assertThat(response.personalChallenges()).hasSize(1);
        assertThat(response.personalChallenges().get(0).title()).isEqualTo(challenge.getTitle());
    }

    @DisplayName("해당 요일에 챌린지가 없으면 예외를 던진다")
    @Test
    void getByDayOfWeek_withNoChallenges_throwsException() {
        // given
        given(challengeRepository.findAllByDayOfWeek(DayOfWeek.TUESDAY)).willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> readService.getByDayOfWeek(DayOfWeek.TUESDAY))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.PERSONAL_CHALLENGE_READ_FAILED.getMessage());
    }

    @DisplayName("회원이 챌린지 상세를 조회할 수 있다")
    @Test
    void getChallengeDetail_withValidMember_returnsDetail() {
        // given
        Long challengeId = 1L;
        Long memberId = 100L;

        PersonalChallenge challenge = PersonalChallengeFixture.of("독서하기");
        ReflectionTestUtils.setField(challenge, "id", challengeId);

        List<PersonalChallengeExampleImage> images = PersonalChallengeExampleImageFixture.list(challenge);
        challenge.getExampleImages().addAll(images);

        given(challengeRepository.findById(challengeId)).willReturn(Optional.of(challenge));
        given(verificationRepository.findTopByMemberIdAndPersonalChallengeIdAndCreatedAtBetween(
                anyLong(), anyLong(), any(), any()))
                .willReturn(Optional.of(PersonalChallengeVerificationFixture.of(MemberFixture.of(), challenge)));

        // when
        PersonalChallengeDetailResponseDto response = readService.getChallengeDetail(memberId, challengeId);

        // then
        assertThat(response.status()).isEqualTo(ChallengeStatus.SUCCESS);
        assertThat(response.title()).isEqualTo(challenge.getTitle());
        assertThat(response.exampleImages()).hasSize(2);
    }

    @DisplayName("존재하지 않는 챌린지 ID로 상세조회 시 예외를 던진다")
    @Test
    void getChallengeDetail_withInvalidId_throwsException() {
        // given
        given(challengeRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> readService.getChallengeDetail(1L, 999L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.PERSONAL_CHALLENGE_DETAIL_READ_FAILED.getMessage());
    }

    @DisplayName("비회원은 인증 상태 없이 상세 정보를 조회할 수 있다")
    @Test
    void getChallengeDetail_withNullMemberId_returnsDefaultStatus() {
        // given
        Long challengeId = 1L;
        PersonalChallenge challenge = PersonalChallengeFixture.of("명상하기");
        ReflectionTestUtils.setField(challenge, "id", challengeId);

        List<PersonalChallengeExampleImage> images = PersonalChallengeExampleImageFixture.list(challenge);
        challenge.getExampleImages().addAll(images);

        given(challengeRepository.findById(challengeId)).willReturn(Optional.of(challenge));

        // when
        PersonalChallengeDetailResponseDto response = readService.getChallengeDetail(null, challengeId);

        // then
        assertThat(response.status()).isEqualTo(ChallengeStatus.NOT_SUBMITTED);
        assertThat(response.exampleImages()).hasSize(2);
    }

    @DisplayName("챌린지 규칙 조회 시 예시 이미지와 인증 시간을 반환한다")
    @Test
    void getChallengeRules_withValidChallenge_returnsRuleInfo() {
        // given
        Long challengeId = 1L;
        PersonalChallenge challenge = PersonalChallengeFixture.of("아침 조깅하기");
        ReflectionTestUtils.setField(challenge, "id", challengeId);

        List<PersonalChallengeExampleImage> images = PersonalChallengeExampleImageFixture.list(challenge);
        challenge.getExampleImages().addAll(images);

        given(challengeRepository.findById(challengeId)).willReturn(Optional.of(challenge));

        // when
        PersonalChallengeRuleResponseDto response = readService.getChallengeRules(challengeId);

        // then
        assertThat(response.certificationPeriod().dayOfWeek()).isEqualTo(challenge.getDayOfWeek());
        assertThat(response.exampleImages()).hasSize(2);
    }

    @DisplayName("존재하지 않는 챌린지 규칙 요청 시 예외를 던진다")
    @Test
    void getChallengeRules_withInvalidId_throwsException() {
        // given
        given(challengeRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> readService.getChallengeRules(999L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ChallengeErrorCode.PERSONAL_CHALLENGE_RULE_NOT_FOUND.getMessage());
    }
}
