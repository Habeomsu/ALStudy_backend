package main.als.websocket.converter;

import jakarta.persistence.GeneratedValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import main.als.websocket.dto.MessageRequestDto;
import main.als.websocket.dto.MessageResponseDto;
import main.als.websocket.entity.Message;
import org.checkerframework.checker.units.qual.A;

import java.time.LocalDateTime;


public class MessageConverter {


    public static Message toMessage(MessageRequestDto.MessageDto messageDto){

        Message message = Message.builder()
                .type(messageDto.getType())
                .sender(messageDto.getSender())
                .channelId(messageDto.getChannelId())
                .data(messageDto.getData())
                .createdAt(LocalDateTime.now())
                .build();

        return message;
    }

    public static MessageResponseDto.MessageDto toMessageDto(Message message){
        MessageResponseDto.MessageDto messageDto = MessageResponseDto.MessageDto.builder()
                .sender(message.getSender())
                .channelId(message.getChannelId())
                .data(message.getData())
                .createdAt(message.getCreatedAt())
                .build();

        return messageDto;
    }
}
