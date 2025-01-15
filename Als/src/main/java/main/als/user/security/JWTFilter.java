package main.als.user.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.als.apiPayload.ApiResult;
import main.als.apiPayload.code.status.ErrorStatus;
import main.als.user.dto.CustomUserDetails;
import main.als.user.entity.Role;
import main.als.user.entity.User;
import main.als.user.util.JsonResponseUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.io.PrintWriter;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {

        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = request.getHeader("access");

        String requestURI = request.getRequestURI();
        if (requestURI.equals("/reissue")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (requestURI.equals("/join")){
            filterChain.doFilter(request, response);
            return;
        }


        if(accessToken == null) {

            filterChain.doFilter(request, response);

            return;
        }

        try{
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e){
            // API 응답 생성
            ApiResult<?> apiResult = ApiResult.onFailure(ErrorStatus._EXFIRED_ACCESS_TOKEN.getCode(), ErrorStatus._EXFIRED_ACCESS_TOKEN.getMessage(), null);
            JsonResponseUtil.sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, apiResult);
            return;
        }

        String category = jwtUtil.getCategory(accessToken);

        if (!category.equals("access")) {

            ApiResult<?> apiResult = ApiResult.onFailure(ErrorStatus._INVALID_ACCESS_TOKEN.getCode(), ErrorStatus._INVALID_ACCESS_TOKEN.getMessage(), null);
            JsonResponseUtil.sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, apiResult);
            return;
        }


        String username= jwtUtil.getUsername(accessToken);
        String role= jwtUtil.getRole(accessToken);

        User user = User.builder()
                .username(username)
                .role(Role.valueOf(role))
                .build();

        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);

    }
}
