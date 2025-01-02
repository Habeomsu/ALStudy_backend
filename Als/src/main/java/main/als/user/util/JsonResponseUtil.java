package main.als.user.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import main.als.apiPayload.ApiResult;

import java.io.IOException;
import java.io.PrintWriter;

public class JsonResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void sendJsonResponse(HttpServletResponse response, int status, ApiResult<?> apiResult) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = objectMapper.writeValueAsString(apiResult);

        // 응답 본문에 JSON 설정
        PrintWriter out = response.getWriter();
        out.write(jsonResponse);
        out.flush();
    }
}
