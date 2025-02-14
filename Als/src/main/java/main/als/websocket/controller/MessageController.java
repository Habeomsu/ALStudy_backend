package main.als.websocket.controller;

import main.als.apiPayload.ApiResult;
import main.als.websocket.dto.MessageResponseDto;
import main.als.websocket.service.MessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/message/{groupId}")
    public ApiResult<MessageResponseDto.SearchMessage> getMessages(@PathVariable String groupId,
                                                                   @RequestParam int page,
                                                                   @RequestParam int size) {

        return ApiResult.onSuccess(messageService.getMessages(groupId, page, size));
    }
}
