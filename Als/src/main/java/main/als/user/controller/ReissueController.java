package main.als.user.controller;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import main.als.apiPayload.ApiResult;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.apiPayload.code.status.SuccessStatus;
import main.als.user.entity.Refresh;
import main.als.user.repository.RefreshRepository;
import main.als.user.security.JWTUtil;
import org.hibernate.StaleObjectStateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class ReissueController {

    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public ReissueController(JWTUtil jwtUtil, RefreshRepository refreshRepository) {

        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @PostMapping("/reissue")
    @Transactional
    public ApiResult<?> reissue(HttpServletRequest request, HttpServletResponse response){

        String refresh = null;
        Cookie[] cookies = request.getCookies();
        // 쿠키가 null인지 확인
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    refresh = cookie.getValue();
                }
            }
        }
        if (refresh == null) {

            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            //response status code
            return ApiResult.onFailure(ErrorStatus._NOTFOUND_REFRESH_TOKEN.getCode(),ErrorStatus._NOTFOUND_REFRESH_TOKEN.getMessage(), null);
        }

        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return ApiResult.onFailure(ErrorStatus._EXFIRED_REFRESH_TOKEN.getCode(),ErrorStatus._EXFIRED_REFRESH_TOKEN.getMessage(), null);
        }catch (JwtException e) {
            // 기타 JWT 관련 오류 처리
            return ApiResult.onFailure(ErrorStatus._INVALID_REFRESH_TOKEN.getCode(),ErrorStatus._INVALID_REFRESH_TOKEN.getMessage(), null);

        }

        // 토큰이 refresh인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(refresh);

        if (!category.equals("refresh")) {

            //response status code
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return ApiResult.onFailure(ErrorStatus._INVALID_REFRESH_TOKEN.getCode(),ErrorStatus._INVALID_REFRESH_TOKEN.getMessage(),null);
        }

        Boolean isExist = refreshRepository.existsByRefresh(refresh);
        if (!isExist) {
            //response body
            return ApiResult.onFailure(ErrorStatus._NOTFOUND_REFRESH_TOKEN.getCode(),ErrorStatus._NOTFOUND_REFRESH_TOKEN.getMessage(),null);
        }

        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        //make new JWT
        String newAccess = jwtUtil.createJwt("access", username, role, 600000L);
        String newRefresh = jwtUtil.createJwt("refresh", username, role, 86400000L);

        refreshRepository.deleteByUsername(username);
        addRefresh(username, newRefresh, 86400000L);

        //response
        response.setHeader("access", newAccess);
        response.addCookie(createCookie("refresh", newRefresh));


        return ApiResult.onSuccess(SuccessStatus._OK);


    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24*60*60);
        //cookie.setSecure(true);
        // cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

    private void addRefresh(String username, String refresh, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        Refresh refresh_data = Refresh.builder()
                .username(username)
                .refresh(refresh)
                .expiration(date.toString())
                .build();

        refreshRepository.save(refresh_data);
    }

}
