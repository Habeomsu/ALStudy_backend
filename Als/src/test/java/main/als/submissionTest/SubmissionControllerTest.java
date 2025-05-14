package main.als.submissionTest;

import main.als.page.PagingConverter;
import main.als.page.PostPagingDto;
import main.als.problem.controller.SubmissionController;
import main.als.problem.dto.SubmissionResponseDto;
import main.als.problem.service.SubmissionService;
import main.als.user.dto.CustomUserDetails;
import main.als.user.entity.Role;
import main.als.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubmissionController.class)
@WithMockUser(username = "test")
public class SubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubmissionService submissionService;

    @Test
    @DisplayName("getAllSubmission 테스트")
    public void getAllSubmission() throws Exception {

        String username = "test";
        Long groupProblemId = 1L;

        User user = User.builder().id(1L)
                .username(username)
                .password("test")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        int page = 1;
        int size = 10;
        String sort = "DESC";

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .build();

        SubmissionResponseDto.AllSubmissionDto submission1 = SubmissionResponseDto.AllSubmissionDto.builder()
                .id(1L)
                .groupProblemId(groupProblemId)
                .build();

        SubmissionResponseDto.SearchSubmissionDto mockedDto = SubmissionResponseDto.SearchSubmissionDto.builder()
                .submissionResDtos(List.of(submission1))
                .isFirst(true)
                .isLast(false)
                .totalElements(1)
                .listSize(1)
                .build();

        try(MockedStatic<PagingConverter> mockedStatic = Mockito.mockStatic(PagingConverter.class)) {
            mockedStatic.when(()->PagingConverter.toPagingDto(page,size,sort)).thenReturn(pagingDto);
            when(submissionService.getAll(groupProblemId,username,pagingDto)).thenReturn(mockedDto);

            mockMvc.perform(get("/submission/{groupProblemId}",groupProblemId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("page", String.valueOf(page))
                    .param("size", String.valueOf(size))
                    .param("sort", sort)
                    .with(csrf())
                    .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.submissionResDtos").isArray())
                    .andExpect(jsonPath("$.result.submissionResDtos[0].groupProblemId").value(1L))
                    .andExpect(jsonPath("$.result.first").value(true));

        }

    }

    @Test
    @DisplayName("getDetailSubmission 테스트")
    public void getDetailSubmission() throws Exception {

        String username = "test";
        Long groupProblemId = 1L;
        Long submissionId = 1L;

        User user = User.builder().id(1L)
                .username(username)
                .password("test")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SubmissionResponseDto.SubmissionDto mockedDto = SubmissionResponseDto.SubmissionDto.builder()
                .id(1L)
                .groupProblemId(groupProblemId)
                .build();

        when(submissionService.getSubmission(groupProblemId,submissionId,username)).thenReturn(mockedDto);

        mockMvc.perform(get("/submission/{groupProblemId}/{submissionId}",groupProblemId,submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."))
                .andExpect(jsonPath("$.result.groupProblemId").value(1L));

    }

    @Test
    @DisplayName("submit 테스트")
    public void submit() throws Exception {

        String username = "test";
        Long groupProblemId = 1L;

        User user = User.builder().id(1L)
                .username(username)
                .password("test")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        MockMultipartFile file = new MockMultipartFile(
                "file",                       // name
                "solution.py",              // original filename
                "text/plain",                 // content type
                "public class Solution {}".getBytes() // content
        );

        String language = "python";

        MockMultipartFile languagePart = new MockMultipartFile(
                "language",     // name
                "",             // filename (빈 문자열이면 일반 필드처럼 인식됨)
                "text/plain",   // content type
                language.getBytes()
        );

        mockMvc.perform(multipart("/submission/{groupProblemId}", groupProblemId)
                        .file(file)
                        .file(languagePart)
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."));

        verify(submissionService).submit(file,language,groupProblemId,username);

    }

    @Test
    @DisplayName("getOtherAllSubmission 테스트")
    public void getOtherAllSubmission() throws Exception {

        String username = "test";
        Long groupProblemId = 1L;

        User user = User.builder().id(1L)
                .username(username)
                .password("test")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        int page = 1;
        int size = 10;
        String sort = "DESC";

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .build();

        SubmissionResponseDto.OtherAllSubmissionDto submissionDto = SubmissionResponseDto.OtherAllSubmissionDto.builder()
                .id(1L)
                .groupProblemId(groupProblemId)
                .build();

        SubmissionResponseDto.SearchOtherSubmissionDto mockedDto = SubmissionResponseDto.SearchOtherSubmissionDto.builder()
                .otherSubmissionResDtos(List.of(submissionDto))
                .isFirst(true)
                .isLast(true)
                .listSize(1)
                .totalElements(1)
                .build();

        try(MockedStatic<PagingConverter> mockedStatic = Mockito.mockStatic(PagingConverter.class)) {
            mockedStatic.when(()->PagingConverter.toPagingDto(page,size,sort)).thenReturn(pagingDto);
            when(submissionService.getOtherAll(groupProblemId,username,pagingDto)).thenReturn(mockedDto);

            mockMvc.perform(get("/submission/others/{groupProblemId}",groupProblemId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size))
                            .param("sort", sort)
                            .with(csrf())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."))
                    .andExpect(jsonPath("$.result.otherSubmissionResDtos").isArray())
                    .andExpect(jsonPath("$.result.otherSubmissionResDtos[0].groupProblemId").value(1L))
                    .andExpect(jsonPath("$.result.first").value(true));

        }

    }

    @Test
    @DisplayName("getOtherSubmission 테스트")
    public void getOtherSubmission() throws Exception {

        String username = "test";
        Long groupProblemId = 1L;
        Long submissionId = 1L;

        User user = User.builder().id(1L)
                .username(username)
                .password("test")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SubmissionResponseDto.OtherSubmissionDto mockedDto = SubmissionResponseDto.OtherSubmissionDto.builder()
                .id(1L)
                .groupProblemId(groupProblemId)
                .build();

        when(submissionService.getOtherSubmission(groupProblemId,submissionId,username)).thenReturn(mockedDto);

        mockMvc.perform(get("/submission/others/{groupProblemId}/{submissionId}",groupProblemId,submissionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."))
                .andExpect(jsonPath("$.result.groupProblemId").value(1L));


    }

}
