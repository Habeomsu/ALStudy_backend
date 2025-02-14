package main.als.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class MessageResponseDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MessageDto{

        private String sender;
        private String channelId;
        private Object data;
        private LocalDateTime createdAt;

    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchMessage{
        List<MessageDto> messageResDtos;
        boolean isFirst;
        boolean isLast;
        int listSize;
        long totalElements;
    }



}
