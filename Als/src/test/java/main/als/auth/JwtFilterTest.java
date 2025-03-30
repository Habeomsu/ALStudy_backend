package main.als.user.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtFilterTest {

    @InjectMocks
    private JWTFilter jwtFilter;

    @Mock
    private JWTUtil jwtUtil;

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
        jwtFilter = new JWTFilter(jwtUtil);
        responseContent = new ByteArrayOutputStream();
        writer = new PrintWriter(responseContent);
        objectMapper = new ObjectMapper();

        lenient().when(response.getWriter()).thenReturn(writer);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("jwt 필터 테스트 성공")
    public void jwtFilterSuccessTest() throws Exception {

        when(request.getHeader(anyString())).thenReturn("access");
        when(request.getRequestURI()).thenReturn("/api/some");
        when(jwtUtil.isExpired(anyString())).thenReturn(false);
        when(jwtUtil.getCategory(anyString())).thenReturn("access");
        when(jwtUtil.getUsername(anyString())).thenReturn("username");
        when(jwtUtil.getRole(anyString())).thenReturn("ROLE_USER");

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("username", SecurityContextHolder.getContext().getAuthentication().getName());

        verify(filterChain).doFilter(request, response);


    }

    @Test
    @DisplayName("엑세스 토큰 Null인 경우 테스트")
    public void jwtFilterFail1Test() throws Exception {

        when(request.getHeader(anyString())).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/some");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("엑세스 토큰이 만료된 경우 테스트")
    public void jwtFilterFail2Test() throws Exception {

        when(request.getHeader(anyString())).thenReturn("access");
        when(request.getRequestURI()).thenReturn("/api/some");
        doThrow(new ExpiredJwtException(null, null, "expired"))
                .when(jwtUtil).isExpired("access");

        jwtFilter.doFilterInternal(request, response, filterChain);
        writer.flush();

        JsonNode json = objectMapper.readTree(responseContent.toString());
        assertFalse(json.path("isSuccess").asBoolean());
        assertEquals("JWT400_1", json.path("code").asText());
        assertEquals("만료된 access 토큰입니다.",json.path("message").asText());

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    @DisplayName("토큰이 엑세스 토큰이 아닌 경우 테스트")
    public void jwtFilterFail3Test() throws Exception {

        when(request.getHeader(anyString())).thenReturn("access");
        when(request.getRequestURI()).thenReturn("/api/some");
        when(jwtUtil.isExpired(anyString())).thenReturn(false);
        when(jwtUtil.getCategory(anyString())).thenReturn("NotAccess");

        jwtFilter.doFilterInternal(request, response, filterChain);
        writer.flush();

        JsonNode json = objectMapper.readTree(responseContent.toString());
        assertFalse(json.path("isSuccess").asBoolean());
        assertEquals("JWT400_2", json.path("code").asText());
        assertEquals("유효하지 않는 access 토큰입니다.",json.path("message").asText());

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

}
