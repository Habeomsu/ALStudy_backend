package main.als.usertest;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.als.user.controller.DeleteController;
import main.als.user.controller.JoinController;
import main.als.user.dto.CustomUserDetails;
import main.als.user.dto.JoinDto;
import main.als.user.entity.Role;
import main.als.user.entity.User;
import main.als.user.service.DeleteService;
import main.als.user.service.JoinService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;


import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.verify;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = {JoinController.class,DeleteController.class})
@WithMockUser(username = "test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JoinService joinService;

    @MockitoBean
    private DeleteService deleteService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 요청 컨트롤러 서비스")
    public void JoinContollerTest() throws Exception {

        JoinDto joinDto = new JoinDto();
        joinDto.setUsername("username");
        joinDto.setPassword("password");

        String requestBody = objectMapper.writeValueAsString(joinDto);

        // Mock joinService: 실제 서비스 로직을 호출하지 않고 아무 일도 하지 않도록 설정
        doNothing().when(joinService).joinProcess(Mockito.any(JoinDto.class));

        ResultActions resultActions = mockMvc.perform(post("/join")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                );

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."));

        // Verify: JoinService의 joinProcess 메소드가 제대로 호출되었는지 확인
        verify(joinService).joinProcess(Mockito.any(JoinDto.class));
    }

    @Test
    @DisplayName("사용자 삭제 서비스")
    public void DeleteControllerTest() throws Exception {

        User user = new User();
        user.setUsername("testUser");
        user.setPassword("password");
        user.setRole(Role.ROLE_USER);
        user.setCustomerId("customerId");

        // CustomUserDetails 객체 생성
        CustomUserDetails userDetails = new CustomUserDetails(user);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        doNothing().when(deleteService).deleteUser(Mockito.anyString());

        // When: DELETE 요청을 보내기
        ResultActions resultActions = mockMvc.perform(delete("/resign")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
        );

        // Then: 결과 확인
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."));

        // Verify: deleteService의 deleteUser 메소드가 호출되었는지 확인
        verify(deleteService).deleteUser(user.getUsername());

    }

}
