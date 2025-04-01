package main.als.grouptest;


import com.fasterxml.jackson.databind.ObjectMapper;
import main.als.group.controller.GroupController;
import main.als.group.dto.GroupRequestDto;
import main.als.group.dto.GroupResponseDto;
import main.als.group.service.GroupService;
import main.als.payment.dto.PaymentRequestDto;
import main.als.user.dto.CustomUserDetails;
import main.als.user.entity.Role;
import main.als.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupController.class)
@WithMockUser(username="test")
public class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroupService groupService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("모든 그룹 가져오기 테스트 - (검색어 없이)")
    public void getAllGroupsNoSearchTest() throws Exception {

        GroupResponseDto.SearchGroups mockResponse = GroupResponseDto.SearchGroups.builder()
                .groupResDtos(List.of())
                .isFirst(true)
                .isLast(false)
                .listSize(0)
                .totalElements(0L)
                .build();

        when(groupService.getAllGroups(any(), isNull())).thenReturn(mockResponse);

        ResultActions resultActions = mockMvc.perform(get("/groups")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "desc")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."))
                .andExpect(jsonPath("$.result.first").value(true))
                .andExpect(jsonPath("$.result.last").value(false))
                .andExpect(jsonPath("$.result.groupResDtos").isArray());

    }

    @Test
    @DisplayName("모든 그룹 가져오기 테스트 - (검색어 포함)")
    public void getAllGroupsSearchTest() throws Exception {

        GroupResponseDto.SearchGroups mockResponse = GroupResponseDto.SearchGroups.builder()
                .groupResDtos(List.of())
                .isFirst(true)
                .isLast(false)
                .listSize(0)
                .totalElements(0L)
                .build();

        when(groupService.getAllGroups(any(), any())).thenReturn(mockResponse);

        ResultActions resultActions = mockMvc.perform(get("/groups")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "desc")
                .param("search", "search")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."))
                .andExpect(jsonPath("$.result.first").value(true))
                .andExpect(jsonPath("$.result.last").value(false))
                .andExpect(jsonPath("$.result.groupResDtos").isArray());

    }

    @Test
    @DisplayName("아이디로 그룹 가져오기 테스트")
    public void getGroupByIdTest() throws Exception {

        GroupResponseDto.AllGroupDto mockDto =  GroupResponseDto.AllGroupDto.builder()
                .id(1L)
                .groupname("test")
                .username("test")
                .depositAmount(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .deadline(LocalDateTime.now().plusDays(1))
                .stutyEndDate(LocalDateTime.now().plusDays(2))
                .build();

        when(groupService.getGroup(any())).thenReturn(mockDto);

        ResultActions resultActions = mockMvc.perform(get("/groups/{groupId}", 1L)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "desc")
                .param("search", "search")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."))
                .andExpect(jsonPath("$.result.id").value(1L))
                .andExpect(jsonPath("$.result.groupname").value("test"));

    }

    @Test
    @DisplayName("그룹 삭제 테스트")
    public void deleteGroupTest() throws Exception {

        Long groupId = 1L;
        String password = "test";
        String username = "test";

        User user =User.builder()
                .id(1L)
                .username(username)
                .password(password)
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResultActions resultActions = mockMvc.perform(delete("/groups/{groupId}", 1L)
                .param("password", password)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."));

        verify(groupService).deleteGroup(groupId, username, password);

    }

    @Test
    @DisplayName("결제를 통한 그룹생성 테스트")
    public void createWithPaymentTest() throws Exception {

        PaymentRequestDto.GroupPaymentDto paymentDto = PaymentRequestDto.GroupPaymentDto.builder()
                .orderId("orderId123")
                .amount("0")
                .paymentKey("paymentKey123")
                .build();

        GroupRequestDto.CreateWithPaymentDto createDto = GroupRequestDto.CreateWithPaymentDto.builder()
                .groupname("test")
                .password("test")
                .depositAmount(BigDecimal.ZERO)
                .deadline(LocalDateTime.now())
                .studyEndDate(LocalDateTime.now().plusDays(1))
                .GroupPaymentDto(paymentDto)
                .build();

        User user = User.builder()
                .id(1L)
                .username("test")
                .password("test")
                .role(Role.ROLE_USER)
                .customerId("customerId")
                .build();

        String requestBody = objectMapper.writeValueAsString(createDto);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResultActions resultActions = mockMvc.perform(post("/groups/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf())
        );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."));

        verify(groupService).createGroupWithPayment(any(GroupRequestDto.CreateWithPaymentDto.class),eq("test"));

    }

}
