package main.als.groupProblem;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.als.page.PagingConverter;
import main.als.page.PostPagingDto;
import main.als.problem.controller.GroupProblemController;
import main.als.problem.dto.GroupProblemRequestDto;
import main.als.problem.dto.GroupProblemResponseDto;
import main.als.problem.service.GroupProblemService;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.securityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupProblemController.class)
@WithMockUser(username = "test")
public class GroupProblemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroupProblemService groupProblemService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("getGroupProblem 테스트")
    public void getGroupProblemTest() throws Exception {

        String username = "test";

        User user = User.builder().id(1L)
                .username(username)
                .password("test")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        Long groupId = 1L;
        int page = 1;
        int size = 10;
        String sort = "DESC";

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .build();

        GroupProblemResponseDto.AllGroupProblem problems = GroupProblemResponseDto.AllGroupProblem.builder()
                .groupProblemId(1L)
                .title("title")
                .build();

        GroupProblemResponseDto.SearchGroupProblem mockedDto = GroupProblemResponseDto.SearchGroupProblem.builder()
                .groupProblemResDtos(new ArrayList<>())
                .isFirst(true)
                .isLast(false)
                .listSize(1)
                .totalElements(1)
                .build();

        mockedDto.getGroupProblemResDtos().add(problems);

        try(MockedStatic<PagingConverter> mockedStatic = Mockito.mockStatic(PagingConverter.class)) {
            mockedStatic.when(()->PagingConverter.toPagingDto(page,size,sort)).thenReturn(pagingDto);
            when(groupProblemService.getGroupProblems(groupId,username,pagingDto)).thenReturn(mockedDto);

            mockMvc.perform(get("/groupproblem/{groupId}",groupId)
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
                    .andExpect(jsonPath("$.result.groupProblemResDtos").isArray())
                    .andExpect(jsonPath("$.result.groupProblemResDtos[0].groupProblemId").value(1L))
                    .andExpect(jsonPath("$.result.first").value(true));

        }

    }

    @Test
    @DisplayName("getTodayGroupProblem 테스트")
    public void getTodayGroupProblemTest() throws Exception {

        String username = "test";

        User user = User.builder().id(1L)
                .username(username)
                .password("test")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        Long groupId = 1L;
        int page = 1;
        int size = 10;
        String sort = "DESC";

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .build();

        GroupProblemResponseDto.AllGroupProblem problems = GroupProblemResponseDto.AllGroupProblem.builder()
                .groupProblemId(1L)
                .title("title")
                .build();

        GroupProblemResponseDto.SearchGroupProblem mockedDto = GroupProblemResponseDto.SearchGroupProblem.builder()
                .groupProblemResDtos(new ArrayList<>())
                .isFirst(true)
                .isLast(false)
                .listSize(1)
                .totalElements(1)
                .build();

        mockedDto.getGroupProblemResDtos().add(problems);

        try(MockedStatic<PagingConverter> mockedStatic = Mockito.mockStatic(PagingConverter.class)) {
            mockedStatic.when(()->PagingConverter.toPagingDto(page,size,sort)).thenReturn(pagingDto);
            when(groupProblemService.getTodayGroupProblems(groupId,username,pagingDto)).thenReturn(mockedDto);

            mockMvc.perform(get("/groupproblem/{groupId}/todayProblem",groupId)
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
                    .andExpect(jsonPath("$.result.groupProblemResDtos").isArray())
                    .andExpect(jsonPath("$.result.groupProblemResDtos[0].groupProblemId").value(1L))
                    .andExpect(jsonPath("$.result.first").value(true));

        }

    }

    @Test
    @DisplayName("getDetailGroupProblem 테스트")
    public void getDetailGroupProblemTest() throws Exception {

        Long groupId = 1L;
        Long groupProblemId = 1L;
        String username = "test";

        User user = User.builder().id(1L)
                .username(username)
                .password("test")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        GroupProblemResponseDto.DetailGroupProblem mockedDto = GroupProblemResponseDto.DetailGroupProblem.builder()
                .groupProblemId(groupProblemId)
                .problemId(1L)
                .title("title")
                .build();

        when(groupProblemService.getDetailGroupProblem(groupId,groupProblemId,username)).thenReturn(mockedDto);

        mockMvc.perform(get("/groupproblem/{groupId}/{groupProblemId}",groupId,groupProblemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."))
                .andExpect(jsonPath("$.result.groupProblemId").value(groupProblemId))
                .andExpect(jsonPath("$.result.problemId").value(1L))
                .andExpect(jsonPath("$.result.title").value("title"));

    }

    @Test
    @DisplayName("DeleteGroupProblem 테스트")
    public void deleteGroupProblemTest() throws Exception {

        Long groupId = 1L;
        Long groupProblemId = 1L;
        String username = "test";

        User user = User.builder().id(1L)
                .username(username)
                .password("test")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        mockMvc.perform(delete("/groupproblem/{groupId}/{groupProblemId}",groupId,groupProblemId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"));

        verify(groupProblemService,times(1)).deleteGroupProblem(groupId,groupProblemId,username);

    }

    @Test
    @DisplayName("createGroupProblem 테스트")
    public void createGroupProblemTest() throws Exception {

        Long groupId = 1L;
        Long groupProblemId = 1L;
        String username = "test";

        LocalDateTime now = LocalDateTime.now();

        User user = User.builder().id(1L)
                .username(username)
                .password("test")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        GroupProblemRequestDto.GroupProblemDto groupProblemDto = GroupProblemRequestDto.GroupProblemDto.builder()
                .problem_id(1L)
                .deadline(now.plusDays(1))
                .deductionAmount(BigDecimal.valueOf(1000))
                .build();

        String requestBody = objectMapper.writeValueAsString(groupProblemDto);

        mockMvc.perform(post("/groupproblem/{groupId}",groupId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"));

        verify(groupProblemService,times(1)).createGroupProblem(any(),eq(username),eq(groupId));

    }

    @Test
    @DisplayName("UpdateGroupProblem 테스트")
    public void updateGroupProblemTest() throws Exception {

        Long groupId = 1L;
        Long groupProblemId = 1L;

        String username = "test";

        LocalDateTime now = LocalDateTime.now();

        User user = User.builder().id(1L)
                .username(username)
                .password("test")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        GroupProblemRequestDto.UpdateGroupProblemDto updateGroupProblemDto = GroupProblemRequestDto.UpdateGroupProblemDto.builder()
                .deadline(now.plusDays(2))
                .deductionAmount(BigDecimal.valueOf(1000))
                .build();

        String requestBody = objectMapper.writeValueAsString(updateGroupProblemDto);

        mockMvc.perform(put("/groupproblem/{groupId}/{groupProblemId}",groupId,groupProblemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"));

    }


}
