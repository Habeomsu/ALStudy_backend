package main.als.user.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.als.user.dto.CustomUserDetails;
import main.als.user.entity.Refresh;
import main.als.user.entity.Role;
import main.als.user.entity.User;
import main.als.user.repository.RefreshRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginFilterTest {

    @InjectMocks
    private LoginFilter loginFilter;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private RefreshRepository refreshRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Authentication authentication;

    private ByteArrayOutputStream responseContent;
    private PrintWriter writer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        loginFilter = new LoginFilter(authenticationManager, jwtUtil, refreshRepository);
        responseContent = new ByteArrayOutputStream();
        writer = new PrintWriter(responseContent);
        objectMapper = new ObjectMapper();

        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void successfulAuthentication() throws Exception {
        // given
        String username = "testuser";
        String customerId = "CID123";
        String role = "ROLE_USER";
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        User user = User.builder()
                .username(username)
                .customerId(customerId)
                .role(Role.ROLE_USER)
                .build();

        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        when(authentication.getPrincipal()).thenReturn(customUserDetails);


        doReturn(Collections.singletonList(new SimpleGrantedAuthority(role)))
                .when(authentication).getAuthorities();

        when(jwtUtil.createJwt("access", username, role, 600000L)).thenReturn(accessToken);
        when(jwtUtil.createJwt("refresh", username, role, 86400000L)).thenReturn(refreshToken);

        // when
        loginFilter.successfulAuthentication(request, response, filterChain, authentication);
        writer.flush();

        // then: 헤더, 쿠키, DB 저장 확인
        verify(response).setHeader("access", accessToken);
        verify(response).addCookie(any(Cookie.class));
        verify(refreshRepository).save(any(Refresh.class));

        // then: 응답 JSON 파싱 및 값 검증
        String responseBody = responseContent.toString();
        JsonNode json = objectMapper.readTree(responseBody);

        assertTrue(json.get("isSuccess").asBoolean());
        assertEquals("COMMON200", json.get("code").asText());

        JsonNode result = json.get("result");
        assertEquals(username, result.get("username").asText());
        assertEquals(role, result.get("role").asText());
        assertEquals(customerId, result.get("customerId").asText());
    }

    @Test
    @DisplayName("로그인 실패 테스트")
    void unsuccessfulAuthentication() throws Exception {
        // when
        loginFilter.unsuccessfulAuthentication(
                request,
                response,
                new BadCredentialsException("Invalid credentials")
        );
        writer.flush();

        // then
        verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
        String responseBody = responseContent.toString();
        JsonNode json = objectMapper.readTree(responseBody);

        assertFalse(json.get("isSuccess").asBoolean());
        assertEquals("USER400_2", json.get("code").asText());
        assertEquals("회원가입된 아이디가 아닙니다.",json.get("message").asText());

    }
}
