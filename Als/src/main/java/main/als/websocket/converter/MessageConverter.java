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
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


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

    public static List<MessageResponseDto.MessageDto> toMessageDto(List<Message> messages){
        return messages.stream()
                .map(MessageConverter::toMessageDto)
                .collect(Collectors.toList());
    }

    public static MessageResponseDto.SearchMessage toSearchMessage(Page<Message> messages){
        return MessageResponseDto.SearchMessage.builder()
                .messageResDtos(toMessageDto(messages.getContent()))
                .isFirst(messages.isFirst())
                .isLast(messages.isLast())
                .listSize(messages.getTotalPages())
                .totalElements(messages.getTotalElements())
                .build();
    }

}
