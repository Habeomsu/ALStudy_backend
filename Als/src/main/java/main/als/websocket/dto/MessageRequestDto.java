package main.als.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class MessageRequestDto {

    @Getter
    @Builder
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class MessageDto{
        private String type;
        private String sender;
        private String channelId;
        private Object data;
    }


}
