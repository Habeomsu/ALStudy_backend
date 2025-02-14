package main.als.websocket.controller;

import lombok.RequiredArgsConstructor;
import main.als.websocket.converter.MessageConverter;
import main.als.websocket.dto.MessageRequestDto;
import main.als.websocket.entity.Message;
import main.als.websocket.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessageSendingOperations simpMessageSendingOperations;
    private final MessageService messageService;

    @MessageMapping("/hello")
    public void message(MessageRequestDto.MessageDto messageDto) {

        Message message = MessageConverter.toMessage(messageDto);
        Message newMessage = messageService.createMessage(message);

        simpMessageSendingOperations.convertAndSend("/sub/channel/" + newMessage.getChannelId(), MessageConverter.toMessageDto(newMessage));
    }
}
