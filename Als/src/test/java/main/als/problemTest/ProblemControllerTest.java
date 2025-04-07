package main.als.problemTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.als.problem.controller.ProblemController;
import main.als.problem.dto.ProblemRequestDto;
import main.als.problem.dto.ProblemResponseDto;
import main.als.problem.entity.ProblemType;
import main.als.problem.service.ProblemService;
import org.checkerframework.checker.interning.qual.FindDistinct;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ProblemController.class)
@WithMockUser(username="test")
public class ProblemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProblemService problemService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("allProblems 테스트")
    public void allProblemsTest() throws Exception {

        ProblemResponseDto.SearchProblems mockResponse = ProblemResponseDto.SearchProblems.builder()
                .problemResDtos(new ArrayList<>())
                .isFirst(true)
                .isLast(false)
                .listSize(3)
                .totalElements(10)
                .build();

        when(problemService.getAllProblems(any(),any(),any())).thenReturn(mockResponse);

        mockMvc.perform(get("/problems")
                .param("page", "0")
                .param("size", "10")
                .param("sort","desc")
                .param("problemType","problems")
                .param("search","search")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.first").value(true))
                    .andExpect(jsonPath("$.result.last").value(false))
                    .andExpect(jsonPath("$.result.problemResDtos").isArray());

    }

    @Test
    @DisplayName("problemById 테스트")
    public void problemByIdTest() throws Exception {

        Long problemId = 1L;

        ProblemResponseDto.ProblemDto mockedDto = ProblemResponseDto.ProblemDto.builder()
                .id(problemId)
                .title("title")
                .build();

        when(problemService.getProblemById(problemId)).thenReturn(mockedDto);

        mockMvc.perform(get("/problems/{problemId}", problemId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."))
                .andExpect(jsonPath("$.result.id").value(1))
                .andExpect(jsonPath("$.result.title").value("title"));

    }

    @Test
    @DisplayName("createProblem 테스트")
    public void createProblemTest() throws Exception {

        ProblemRequestDto.createProblemDto mockedDto = ProblemRequestDto.createProblemDto.builder()
                .title("title")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .description("description")
                .inputDescription("inputDescription")
                .outputDescription("outputDescription")
                .exampleInput("exampleInput")
                .exampleOutput("exampleOutput")
                .build();

        String requestBody = objectMapper.writeValueAsString(mockedDto);

        mockMvc.perform(post("/problems")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."));

        verify(problemService,times(1)).createProblem(any());

    }

    @Test
    @DisplayName("updateProblem 테스트")
    public void updateProblemTest() throws Exception {

        Long problemId = 1L;

        ProblemRequestDto.createProblemDto mockedDto = ProblemRequestDto.createProblemDto.builder()
                .title("title")
                .difficultyLevel("easy")
                .problemType(ProblemType.GREEDY)
                .description("description")
                .inputDescription("inputDescription")
                .outputDescription("outputDescription")
                .exampleInput("exampleInput")
                .exampleOutput("exampleOutput")
                .build();

        String requestBody = objectMapper.writeValueAsString(mockedDto);

        mockMvc.perform(put("/problems/{problemId}", problemId)
                .content(requestBody)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."));

        verify(problemService,times(1)).updateProblem(any(),any());

    }

    @Test
    @DisplayName("deleteProblem 테스트")
    public void deleteProblemTest() throws Exception {

        Long problemId = 1L;

        mockMvc.perform(delete("/problems/{problemId}", problemId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."));

        verify(problemService,times(1)).deleteProblem(any());

    }

}
