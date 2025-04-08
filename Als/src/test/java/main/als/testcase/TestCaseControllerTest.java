package main.als.testcase;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.als.problem.controller.TestCaseController;
import main.als.problem.dto.TestCaseRequestDto;
import main.als.problem.dto.TestCaseResponseDto;
import main.als.problem.service.TestCaseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestCaseController.class)
@WithMockUser(username = "test")
public class TestCaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TestCaseService testCaseService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("getTestCases 테스트 - (성공)")
    public void getTestCases() throws Exception {

        Long problemId = 1L;

        TestCaseResponseDto.TestCaseDto testCaseDto = TestCaseResponseDto.TestCaseDto.builder()
                .id(1L)
                .problemId(1L)
                .input("test")
                .expectedOutput("test")
                .build();

        List<TestCaseResponseDto.TestCaseDto> testCaseDtoList  = List.of(testCaseDto);

        when(testCaseService.getTestCasesByProblemId(problemId)).thenReturn(testCaseDtoList);

        mockMvc.perform(get("/testcase/{problemId}",problemId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."))
                .andExpect(jsonPath("$.result[0].id").value(1L))
                .andExpect(jsonPath("$.result[0].problemId").value(1L))
                .andExpect(jsonPath("$.result[0].input").value("test"))
                .andExpect(jsonPath("$.result[0].expectedOutput").value("test"));

    }

    @Test
    @DisplayName("getTestCase 테스트 - (성공)")
    public void getTestCaseTest() throws Exception {

        Long problemId = 1L;
        Long testcaseId = 1L;

        TestCaseResponseDto.TestCaseDto testCaseDto = TestCaseResponseDto.TestCaseDto.builder()
                .id(1L)
                .problemId(1L)
                .input("test")
                .expectedOutput("test")
                .build();

        when(testCaseService.getTestCaseById(testcaseId)).thenReturn(testCaseDto);

        mockMvc.perform(get("/testcase/{problemId}/{testcaseId}",problemId,testcaseId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."))
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.problemId").value(1L))
                .andExpect(jsonPath("$.result.input").value("test"))
                .andExpect(jsonPath("$.result.expectedOutput").value("test"));

    }

    @Test
    @DisplayName("createTestCase 테스트 - (성공)")
    public void createTestCaseTest() throws Exception {

        TestCaseRequestDto.TestCaseDto mockedDto = TestCaseRequestDto.TestCaseDto.builder()
                .input("test")
                .expectedOutput("test")
                .build();

        String responseBody = objectMapper.writeValueAsString(mockedDto);

        Long problemId = 1L;

        mockMvc.perform(post("/testcase/{problemId}",problemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(responseBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."));

        verify(testCaseService,times(1)).createTestCase(any(),any());
    }

    @Test
    @DisplayName("updateTestCase 테스트 - (성공)")
    public void updateTestCaseTest() throws Exception {

        Long problemId = 1L;
        Long testcaseId = 1L;

        TestCaseRequestDto.TestCaseDto mockedDto = TestCaseRequestDto.TestCaseDto.builder()
                .input("test")
                .expectedOutput("test")
                .build();

        String responseBody = objectMapper.writeValueAsString(mockedDto);

        mockMvc.perform(put("/testcase/{problemId}/{testcaseId}",problemId,testcaseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(responseBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."));

        verify(testCaseService,times(1)).updateTestCase(any(),any());
    }

    @Test
    @DisplayName("deleteTestCase 테스트 - (성공)")
    public void deleteTestCaseTest() throws Exception {

        Long problemId = 1L;
        Long testcaseId = 1L;

        mockMvc.perform(delete("/testcase/{problemId}/{testcaseId}",problemId,testcaseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."));

        verify(testCaseService,times(1)).deleteTestCase(any());
    }


}
