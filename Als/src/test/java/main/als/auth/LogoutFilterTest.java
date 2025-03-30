package main.als.auth;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.als.apiPayload.ApiResult;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.user.repository.RefreshRepository;
import main.als.user.security.CustomLogoutFilter;
import main.als.user.security.JWTUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class LogoutFilterTest {

    @InjectMocks
    private CustomLogoutFilter logoutFilter;

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

    private ByteArrayOutputStream responseContent;
    private PrintWriter writer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        logoutFilter = new CustomLogoutFilter(jwtUtil, refreshRepository);
        responseContent = new ByteArrayOutputStream();
        writer = new PrintWriter(responseContent);
        objectMapper = new ObjectMapper();

        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    @DisplayName("로그아웃 성공 테스트")
    public void LogoutSuccessTest() throws Exception {
        when(request.getRequestURI()).thenReturn("/logout");
        when(request.getMethod()).thenReturn("POST");
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("refresh", "refresh")});
        when(jwtUtil.isExpired(anyString())).thenReturn(false);
        when(jwtUtil.getCategory(anyString())).thenReturn("refresh");
        when(refreshRepository.existsByRefresh(anyString())).thenReturn(true);
        String refresh = "refresh";
        when(jwtUtil.getUsername(refresh)).thenReturn("testuser");


        logoutFilter.doFilter(request, response, filterChain);
        writer.flush();

        verify(refreshRepository).deleteByUsername(anyString());
        JsonNode json = objectMapper.readTree(responseContent.toString());
        assertTrue(json.path("isSuccess").asBoolean());
        assertEquals("성공입니다.", json.path("message").asText());
        assertEquals("COMMON200", json.path("code").asText());
    }

    @Test
    @DisplayName("쿠키없음 테스트")
    public void LogoutFail1Test() throws Exception{

        when(request.getRequestURI()).thenReturn("/logout");
        when(request.getMethod()).thenReturn("POST");
        when(request.getCookies()).thenReturn(null);
        logoutFilter.doFilter(request, response, filterChain);
        writer.flush();

        JsonNode json = objectMapper.readTree(responseContent.toString());
        assertFalse(json.path("isSuccess").asBoolean());
        assertEquals("JWT400_3", json.path("code").asText());
        assertEquals("refresh 토큰이 존재하지않습니다.", json.path("message").asText());

    }

    @Test
    @DisplayName("만료된 리프레쉬 토큰 테스트")
    public void LogoutFail2Test() throws Exception{

        when(request.getRequestURI()).thenReturn("/logout");
        when(request.getMethod()).thenReturn("POST");
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("refresh", "refresh")});
        doThrow(new ExpiredJwtException(null, null, "expired"))
                .when(jwtUtil).isExpired("refresh");

        logoutFilter.doFilter(request, response, filterChain);
        writer.flush();

        JsonNode json = objectMapper.readTree(responseContent.toString());
        assertFalse(json.path("isSuccess").asBoolean());
        assertEquals("JWT400_4", json.path("code").asText());
        assertEquals("만료된 refresh 토큰입니다.", json.path("message").asText());

    }

    @Test
    @DisplayName("토큰이 리프레쉬가 아닌 테스트")
    public void LogoutFail3Test() throws Exception{

        when(request.getRequestURI()).thenReturn("/logout");
        when(request.getMethod()).thenReturn("POST");
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("refresh", "refresh")});
        when(jwtUtil.isExpired(anyString())).thenReturn(false);
        when(jwtUtil.getCategory(anyString())).thenReturn("NotRefresh");

        logoutFilter.doFilter(request, response, filterChain);
        writer.flush();

        JsonNode json = objectMapper.readTree(responseContent.toString());
        assertFalse(json.path("isSuccess").asBoolean());
        assertEquals("JWT400_5", json.path("code").asText());
        assertEquals("유효하지 않는 refresh 토큰입니다.", json.path("message").asText());
    }

    @Test
    @DisplayName("디비에 리프레쉬 토큰이 없는 경우 테스트")
    public void LogoutFail4Test() throws Exception{

        when(request.getRequestURI()).thenReturn("/logout");
        when(request.getMethod()).thenReturn("POST");
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("refresh", "refresh")});
        when(jwtUtil.isExpired(anyString())).thenReturn(false);
        when(jwtUtil.getCategory(anyString())).thenReturn("refresh");
        when(refreshRepository.existsByRefresh(anyString())).thenReturn(false);

        logoutFilter.doFilter(request, response, filterChain);
        writer.flush();

        JsonNode json = objectMapper.readTree(responseContent.toString());
        assertFalse(json.path("isSuccess").asBoolean());
        assertEquals("JWT400_6", json.path("code").asText());
        assertEquals("DB에 refresh 토큰이 존재하지 않습니다.", json.path("message").asText());

    }


}
