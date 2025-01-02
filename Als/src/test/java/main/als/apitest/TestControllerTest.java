package main.als.apitest;

import main.als.testController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(testController.class) //지정된 컨트롤러만 테스트 가능
public class TestControllerTest {

    @Autowired //http요청을 모의하여 컨트롤러 동작 테스트
    private MockMvc mockMvc;

    @Test
    public void testApiResultSuccess() throws Exception {
        mockMvc.perform(get("/") // 요청 URI
                        .contentType(MediaType.APPLICATION_JSON)) // 요청의 미디어 타입 설정
                .andExpect(status().isOk()) // HTTP 200 응답 검증
                .andExpect(jsonPath("$.isSuccess", is(true))) // isSuccess 필드 검증
                .andExpect(jsonPath("$.code", is("COMMON200"))) // 코드 필드 검증
                .andExpect(jsonPath("$.message", is("성공입니다."))) // 메시지 필드 검증
                .andExpect(jsonPath("$.result",is("test성공"))); // result 필드가 존재하지 않음을 검증
    }

}
