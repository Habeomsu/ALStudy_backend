package main.als.websSocketTest;

import main.als.problem.controller.SubmissionController;
import main.als.websocket.controller.MessageController;
import main.als.websocket.dto.MessageResponseDto;
import main.als.websocket.service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@WithMockUser(username = "test")
public class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

    @Test
    @DisplayName("getMessages 테스트")
    public void getMessagesTest() throws Exception {

        String groupId = "test";
        int page = 1;
        int size = 10;

        MessageResponseDto.MessageDto messageDto1 = MessageResponseDto.MessageDto.builder()
                .sender("sender")
                .channelId("channelId")
                .data("data")
                .createdAt(LocalDateTime.now())
                .build();

        MessageResponseDto.MessageDto messageDto2 = MessageResponseDto.MessageDto.builder()
                .sender("sender")
                .channelId("channelId")
                .data("data")
                .createdAt(LocalDateTime.now())
                .build();

        MessageResponseDto.SearchMessage mockedDto = MessageResponseDto.SearchMessage.builder()
                .messageResDtos(List.of(messageDto1, messageDto2))
                .isFirst(true)
                .isLast(false)
                .listSize(2)
                .totalElements(2)
                .build();

        when(messageService.getMessages(groupId, page, size)).thenReturn(mockedDto);

        mockMvc.perform(get("/message/{groupId}",groupId)
                .contentType(MediaType.APPLICATION_JSON)
                .param("page",String.valueOf(page))
                .param("size",String.valueOf(size))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value("COMMON200"))
                .andExpect(jsonPath("$.message").value("성공입니다."))
                .andExpect(jsonPath("$.result.messageResDtos").isArray())
                .andExpect(jsonPath("$.result.messageResDtos[0].channelId").value("channelId"))
                .andExpect(jsonPath("$.result.first").value(true));

    }

}
