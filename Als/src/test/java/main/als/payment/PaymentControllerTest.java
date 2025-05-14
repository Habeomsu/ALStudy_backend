package main.als.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.als.payment.controller.PaymentController;
import main.als.payment.dto.PaymentRequestDto;
import main.als.payment.service.PaymentService;
import main.als.problem.controller.SubmissionController;
import main.als.user.dto.CustomUserDetails;
import main.als.user.entity.Role;
import main.als.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@WithMockUser(username = "test")
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("confirm 테스트")
    public void confirmTest() throws Exception {

        PaymentRequestDto.PaymentDto dto = PaymentRequestDto.PaymentDto.builder()
                .build();

        String requestBody = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/payment/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."));

        verify(paymentService).confirmPayment(any(PaymentRequestDto.PaymentDto.class));

    }

    @Test
    @DisplayName("refund 테스트")
    public void refundTest() throws Exception {

        Long userGroupId = 1L;
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

        mockMvc.perform(get("/payment/refund/{userGroupId}", userGroupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(authentication))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."));

        verify(paymentService).refundPayment(username,userGroupId);

    }
}
