package main.als.websSocketTest;

import main.als.websocket.controller.MessageController;
import main.als.websocket.controller.WebSocketController;
import main.als.websocket.converter.MessageConverter;
import main.als.websocket.dto.MessageRequestDto;
import main.als.websocket.dto.MessageResponseDto;
import main.als.websocket.entity.Message;
import main.als.websocket.service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;


import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WebSocketControllerTest {

    @InjectMocks
    private WebSocketController webSocketController;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @Mock
    private MessageService messageService;

    @Test
    @DisplayName("message 테스트")
    public void MessageTest(){

        MessageRequestDto.MessageDto dto = MessageRequestDto.MessageDto.builder()
                .sender("test-user")
                .channelId("channel-1")
                .data("hello")
                .type("text")
                .build();

        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>());

        Message savedMessage = Message.builder()
                .id(1L)
                .channelId(dto.getChannelId())
                .sender(dto.getSender())
                .type(dto.getType())
                .data(dto.getData())
                .build();

        Message message = MessageConverter.toMessage(dto);

        when(messageService.createMessage(any(Message.class))).thenReturn(savedMessage);

        webSocketController.message(dto, headerAccessor);

        assertEquals("test-user", headerAccessor.getSessionAttributes().get("username"));
        verify(messageService).createMessage(any(Message.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/" + dto.getChannelId()),any(MessageResponseDto.MessageDto.class));

    }

}
