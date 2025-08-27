package ktb.leafresh.backend.global.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrentMemberIdArgumentResolverTest {

    private CurrentMemberIdArgumentResolver resolver;

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private ModelAndViewContainer mavContainer;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private WebDataBinderFactory binderFactory;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resolver = new CurrentMemberIdArgumentResolver();
    }

    @Test
    @DisplayName("CurrentMemberId 어노테이션과 Long 타입을 가진 파라미터를 지원한다")
    void supportsParameter_WithCurrentMemberIdAnnotationAndLongType_ReturnsTrue() {
        // given
        when(methodParameter.hasParameterAnnotation(CurrentMemberId.class)).thenReturn(true);
        when(methodParameter.getParameterType()).thenReturn((Class) Long.class);

        // when
        boolean result = resolver.supportsParameter(methodParameter);

        // then
        assertTrue(result);
    }

    @Test
    @DisplayName("CurrentMemberId 어노테이션이 없는 파라미터는 지원하지 않는다")
    void supportsParameter_WithoutCurrentMemberIdAnnotation_ReturnsFalse() {
        // given
        when(methodParameter.hasParameterAnnotation(CurrentMemberId.class)).thenReturn(false);
        when(methodParameter.getParameterType()).thenReturn((Class) Long.class);

        // when
        boolean result = resolver.supportsParameter(methodParameter);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("Long 타입이 아닌 파라미터는 지원하지 않는다")
    void supportsParameter_WithNonLongType_ReturnsFalse() {
        // given
        when(methodParameter.hasParameterAnnotation(CurrentMemberId.class)).thenReturn(true);
        when(methodParameter.getParameterType()).thenReturn((Class) String.class);

        // when
        boolean result = resolver.supportsParameter(methodParameter);

        // then
        assertFalse(result);
    }

    @Test
    @DisplayName("인증이 없는 경우 null을 반환한다")
    void resolveArgument_WithNoAuthentication_ReturnsNull() {
        // given
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            when(securityContext.getAuthentication()).thenReturn(null);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // when
            Object result = resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);

            // then
            assertNull(result);
        }
    }

    @Test
    @DisplayName("Principal이 CustomUserDetails가 아닌 경우 null을 반환한다")
    void resolveArgument_WithNonCustomUserDetailsPrincipal_ReturnsNull() {
        // given
        String nonCustomUserDetails = "someOtherPrincipal";
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            when(authentication.getPrincipal()).thenReturn(nonCustomUserDetails);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // when
            Object result = resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);

            // then
            assertNull(result);
        }
    }

    @Test
    @DisplayName("올바른 인증 정보가 있는 경우 memberId를 반환한다")
    void resolveArgument_WithValidAuthentication_ReturnsMemberId() {
        // given
        Long expectedMemberId = 12345L;
        
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            when(customUserDetails.getMemberId()).thenReturn(expectedMemberId);
            when(authentication.getPrincipal()).thenReturn(customUserDetails);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // when
            Object result = resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);

            // then
            assertEquals(expectedMemberId, result);
        }
    }

    @Test
    @DisplayName("CustomUserDetails가 null memberId를 반환하는 경우 null을 반환한다")
    void resolveArgument_WithNullMemberId_ReturnsNull() {
        // given
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            when(customUserDetails.getMemberId()).thenReturn(null);
            when(authentication.getPrincipal()).thenReturn(customUserDetails);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // when
            Object result = resolver.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);

            // then
            assertNull(result);
        }
    }
}
