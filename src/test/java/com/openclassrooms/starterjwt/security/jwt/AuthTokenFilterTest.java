package com.openclassrooms.starterjwt.security.jwt;

import com.openclassrooms.starterjwt.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuthTokenFilterTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private AuthTokenFilter createFilter(JwtUtils jwtUtils, UserDetailsServiceImpl uds) {
        AuthTokenFilter filter = new AuthTokenFilter();
        ReflectionTestUtils.setField(filter, "jwtUtils", jwtUtils);
        ReflectionTestUtils.setField(filter, "userDetailsService", uds);
        return filter;
    }

    @Test
    void doFilter_shouldNotAuthenticate_whenNoAuthorizationHeader() throws Exception {
        // Arrange
        JwtUtils jwtUtils = mock(JwtUtils.class);
        UserDetailsServiceImpl uds = mock(UserDetailsServiceImpl.class);
        AuthTokenFilter filter = createFilter(jwtUtils, uds);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtUtils, uds);
    }

    @Test
    void doFilter_shouldNotAuthenticate_whenAuthorizationIsNotBearer() throws Exception {
        // Arrange
        JwtUtils jwtUtils = mock(JwtUtils.class);
        UserDetailsServiceImpl uds = mock(UserDetailsServiceImpl.class);
        AuthTokenFilter filter = createFilter(jwtUtils, uds);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc.def");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtUtils, uds);
    }

    @Test
    void doFilter_shouldNotAuthenticate_whenBearerTokenInvalid() throws Exception {
        // Arrange
        JwtUtils jwtUtils = mock(JwtUtils.class);
        when(jwtUtils.validateJwtToken("bad-token")).thenReturn(false);

        UserDetailsServiceImpl uds = mock(UserDetailsServiceImpl.class);
        AuthTokenFilter filter = createFilter(jwtUtils, uds);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtUtils).validateJwtToken("bad-token");
        verify(jwtUtils, never()).getUserNameFromJwtToken(anyString());
        verifyNoInteractions(uds);
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldAuthenticate_whenBearerTokenValid() throws Exception {
        // Arrange
        JwtUtils jwtUtils = mock(JwtUtils.class);
        when(jwtUtils.validateJwtToken("good-token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("good-token")).thenReturn("user@example.com");

        UserDetailsServiceImpl uds = mock(UserDetailsServiceImpl.class);
        when(uds.loadUserByUsername("user@example.com"))
                .thenReturn(User.withUsername("user@example.com").password("pwd").authorities("USER").build());

        AuthTokenFilter filter = createFilter(jwtUtils, uds);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer good-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("user@example.com");

        verify(jwtUtils).validateJwtToken("good-token");
        verify(jwtUtils).getUserNameFromJwtToken("good-token");
        verify(uds).loadUserByUsername("user@example.com");
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldContinueChain_whenJwtUtilsThrowsException() throws Exception {
        // Arrange
        JwtUtils jwtUtils = mock(JwtUtils.class);
        when(jwtUtils.validateJwtToken("boom")).thenThrow(new RuntimeException("boom"));

        UserDetailsServiceImpl uds = mock(UserDetailsServiceImpl.class);
        AuthTokenFilter filter = createFilter(jwtUtils, uds);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer boom");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }
}
