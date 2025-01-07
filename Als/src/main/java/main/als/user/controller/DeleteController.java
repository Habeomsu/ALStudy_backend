package main.als.user.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import main.als.apiPayload.ApiResult;
import main.als.user.dto.CustomUserDetails;
import main.als.user.service.DeleteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeleteController {

    private final DeleteService deleteService;
    public DeleteController(DeleteService deleteService) {
        this.deleteService = deleteService;
    }

    @DeleteMapping("/resign")
    public ApiResult<?> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletResponse response){
        String username = userDetails.getUsername(); // JWT에서 사용자 ID 가져오기
        deleteService.deleteUser(username);

        // 쿠키 무효화
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0); // 쿠키 만료 시간 설정
        cookie.setPath("/"); // 쿠키 경로 설정
        response.addCookie(cookie); // 쿠키 삭제 요청 추가

        return ApiResult.onSuccess();
    }

}
