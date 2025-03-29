package main.als.reissue;

import jakarta.servlet.http.Cookie;
import main.als.apiPayload.exception.GeneralException;
import main.als.user.controller.ReissueController;
import main.als.user.entity.Refresh;
import main.als.user.repository.RefreshRepository;
import main.als.user.security.JWTUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.servlet.support.WebContentGenerator;

import static main.als.apiPayload.code.status.ErrorStatus._EXFIRED_REFRESH_TOKEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@WebMvcTest(ReissueController.class)
@WithMockUser(username="test1")
public class ReissueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private RefreshRepository refreshRepository;

    private final String VALID_REFRESH_TOKEN = "validToken";
    private final String INVALID_REFRESH_TOKEN = "invalidToken";

    @Test
    @DisplayName("Refresh Token 없음 - 404 Not Found")
    public void noRefreshTest() throws Exception {

        ResultActions resultAction= mockMvc.perform(post("/reissue")
                .with(csrf()));

        resultAction.andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("JWT400_3"))
                .andExpect(jsonPath("$.message").value("refresh 토큰이 존재하지않습니다."));

    }

    @Test
    @DisplayName("Refresh Token 만료됨 - 401 Unauthorized")
    public void expiredTokenTest() throws Exception{

        when(jwtUtil.isExpired(VALID_REFRESH_TOKEN))
                .thenThrow(new GeneralException(_EXFIRED_REFRESH_TOKEN));

        mockMvc.perform(post("/reissue")
                        .cookie(new Cookie("refresh", VALID_REFRESH_TOKEN))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("JWT400_4"))
                .andExpect(jsonPath("$.message").value("만료된 refresh 토큰입니다."));

    }

    @Test
    @DisplayName("Refresh Token이 아님 - 400 Bad")
    public void invalidRefreshTest() throws Exception {

        when(jwtUtil.getCategory(INVALID_REFRESH_TOKEN)).thenReturn("access");

        mockMvc.perform(post("/reissue")
                        .cookie(new Cookie("refresh", INVALID_REFRESH_TOKEN))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value("JWT400_5"))
                .andExpect(jsonPath("$.message").value("유효하지 않는 refresh 토큰입니다."));

    }

    @Test
    @DisplayName("Reissue 성공 테스트")
    public void reissueSuccessTest() throws Exception {

        when(jwtUtil.isExpired(VALID_REFRESH_TOKEN)).thenReturn(false);
        when(jwtUtil.getCategory(VALID_REFRESH_TOKEN)).thenReturn("refresh");
        when(refreshRepository.existsByRefresh(VALID_REFRESH_TOKEN)).thenReturn(true);
        when(jwtUtil.getUsername(VALID_REFRESH_TOKEN)).thenReturn("test1");
        when(jwtUtil.getRole(VALID_REFRESH_TOKEN)).thenReturn("ROLE_USER");
        when(jwtUtil.createJwt("access","test1","ROLE_USER",600000L)).thenReturn("newAccessToken");
        when(jwtUtil.createJwt("refresh","test1","ROLE_USER",86400000L)).thenReturn("newRefreshToken");

        when(refreshRepository.save(any(Refresh.class)))
                .thenReturn(mock(Refresh.class));

        doNothing().when(refreshRepository).deleteByUsername("test1");

        MockHttpServletResponse response = mockMvc.perform(post("/reissue")
                        .cookie(new Cookie("refresh", VALID_REFRESH_TOKEN))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andReturn()
                .getResponse();

        // 응답에 리프레쉬토큰과 엑세스 토큰이 생겼는지 확인
        String accessHeader = response.getHeader("access");
        Cookie[] cookies = response.getCookies();

        // 헤더에 엑세스 토큰 검사
        assertEquals("newAccessToken", accessHeader);

        // 쿠키에서 리프레쉬 토큰 검사
        assertEquals(1, cookies.length);
        assertEquals("refresh", cookies[0].getName());
        assertEquals("newRefreshToken", cookies[0].getValue());

        verify(refreshRepository).deleteByUsername("test1");
        verify(refreshRepository).save(any(Refresh.class));

    }

}








