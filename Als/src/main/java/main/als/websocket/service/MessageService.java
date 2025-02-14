package main.als.websocket.service;


import main.als.websocket.dto.MessageResponseDto;
import main.als.websocket.entity.Message;

public interface MessageService {

    Message createMessage(Message message);
    MessageResponseDto.SearchMessage getMessages(String groupId,int page,int size);
}
