package main.als.userGroupTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.als.group.controller.UserGroupController;
import main.als.group.converter.UserGroupConverter;
import main.als.group.dto.UserGroupRequestDto;
import main.als.group.dto.UserGroupResponseDto;
import main.als.group.entity.UserGroup;
import main.als.group.service.UserGroupService;
import main.als.page.PagingConverter;
import main.als.page.PostPagingDto;
import main.als.user.dto.CustomUserDetails;
import main.als.user.dto.UserDto;
import main.als.user.entity.Role;
import main.als.user.entity.User;
import main.als.user.service.FindUserGroupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserGroupController.class)
@WithMockUser(username = "test")
public class UserGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserGroupService userGroupService;

    @MockitoBean
    private FindUserGroupService findUserGroupService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("유저그룹 가져오기 테스트")
    public void usergroupsTest() throws Exception {

        String username = "test";

        User user = User.builder()
                .username(username)
                .password("password")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);


        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "deadline"));

        Page<UserGroup> mockPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        UserGroupResponseDto.SearchUserGroups responseDto = UserGroupResponseDto.SearchUserGroups.builder()
                .userGroupsResDtos(Collections.emptyList())
                .isFirst(true)
                .isLast(true)
                .listSize(0)
                .totalElements(0L)
                .build();

        when(findUserGroupService.userGroups(Mockito.eq(username), Mockito.any())).thenReturn(mockPage);
        try (MockedStatic<UserGroupConverter> mocked = mockStatic(UserGroupConverter.class)) {
            mocked.when(() -> UserGroupConverter.toSearchUserGroups(mockPage))
                    .thenReturn(responseDto);

            mockMvc.perform(get("/usergroups")
                            .param("page", "0")
                            .param("size", "10")
                            .param("sort", "asc")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.userGroupsResDtos").isArray());
        }
    }

    @Test
    @DisplayName("유저그룹 가입 테스트")
    public void joinUserGroupTest() throws Exception {

        Long groupId = 1L;
        String username = "test";
        String password = "password";

        UserGroupRequestDto.joinGroupDto mockDto = UserGroupRequestDto.joinGroupDto.builder()
                .password(password)
                .build();

        String requestBody = objectMapper.writeValueAsString(mockDto);

        User user = User.builder()
                .username(username)
                .password(password)
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        mockMvc.perform(post("/usergroups/{groupId}", groupId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."));

        verify(userGroupService, times(1)).joinUserGroup(groupId, password, username);

    }

    @Test
    @DisplayName("그룹 탈퇴 테스트")
    public void resignGroupTest() throws Exception{

        Long groupId = 1L;
        String username = "test";
        String password = "password";

        User user= User.builder()
                .username(username)
                .password(password)
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        mockMvc.perform(delete("/usergroups/{groupId}",groupId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."));

        verify(userGroupService, times(1)).resignGroup(groupId, username);

    }

    @Test
    @DisplayName("유저 가지고오기 테스트")
    public void getUsersTest() throws Exception{

        Long groupId = 1L;
        String username = "test";
        String password = "password";
        int page = 1;
        int size = 10;
        String sort = "ASC";

        User user = User.builder()
                .username(username)
                .password(password)
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        UserDto.SearchUsers responseDto = UserDto.SearchUsers.builder()
                .usernameDtos(new ArrayList<>() {
                })
                .isFirst(true)
                .isLast(true)
                .listSize(2)
                .totalElements(2L)
                .build();

        PostPagingDto.PagingDto pagingDto = PostPagingDto.PagingDto.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .build();


        try (MockedStatic<PagingConverter> mocked = mockStatic(PagingConverter.class)) {
            mocked.when(() -> PagingConverter.toPagingDto(page, size, sort)).thenReturn(pagingDto);

            when(userGroupService.getUsersByGroupId(groupId, pagingDto)).thenReturn(responseDto);

            mockMvc.perform(get("/usergroups/{groupId}/users", groupId)
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size))
                            .param("sort", sort)
                            .contentType(MediaType.APPLICATION_JSON)
                            .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.code").value("COMMON200"))
                    .andExpect(jsonPath("$.message").value("성공입니다."));

            verify(userGroupService, times(2)).getUsersByGroupId(groupId, pagingDto);
        }
    }

}

