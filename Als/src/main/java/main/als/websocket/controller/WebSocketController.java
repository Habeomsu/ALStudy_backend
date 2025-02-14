package main.als.websocket.controller;

import lombok.RequiredArgsConstructor;
import main.als.websocket.converter.MessageConverter;
import main.als.websocket.dto.MessageRequestDto;
import main.als.websocket.entity.Message;
import main.als.websocket.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessageSendingOperations simpMessageSendingOperations;
    private final MessageService messageService;

    @MessageMapping("/hello")
    public void message(MessageRequestDto.MessageDto messageDto, SimpMessageHeaderAccessor headerAccessor) {

        Message message = MessageConverter.toMessage(messageDto);

        headerAccessor.getSessionAttributes().put("username", message.getSender());
        Message newMessage = messageService.createMessage(message);

        simpMessageSendingOperations.convertAndSend("/topic/" + newMessage.getChannelId(), MessageConverter.toMessageDto(newMessage));
    }
}
