package main.als.websSocketTest;

import main.als.websocket.converter.MessageConverter;
import main.als.websocket.dto.MessageResponseDto;
import main.als.websocket.entity.Message;
import main.als.websocket.repository.MessageRepository;
import main.als.websocket.service.MessageServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @InjectMocks
    private MessageServiceImpl messageService;

    @Mock
    private MessageRepository messageRepository;

    @Test
    @DisplayName("createMessage 테스트 - (성공)")
    public void createMessageSuccessTest(){

        Message message = Message.builder()
                .channelId("channel-1")
                .sender("user123")
                .type("text")
                .data("Hello!")
                .createdAt(LocalDateTime.now())
                .build();

        Message savedMessage = Message.builder()
                .id(1L)
                .channelId(message.getChannelId())
                .sender(message.getSender())
                .type(message.getType())
                .data(message.getData())
                .createdAt(message.getCreatedAt())
                .build();

        when(messageRepository.save(message)).thenReturn(savedMessage);

        Message result = messageService.createMessage(message);

        assertNotNull(result);
        assertEquals(savedMessage.getId(), result.getId());
        assertEquals(savedMessage.getData(), result.getData());

        verify(messageRepository, times(1)).save(message);

    }

    @Test
    @DisplayName("getMessages 테스트 - (성공)")
    public void getMessagesSuccessTest(){

        String groupId = "groupId";
        int page = 1;
        int size = 10;

        Pageable pageable = PageRequest.of(page, size);

        Message message1 = Message.builder()
                .channelId(groupId)
                .sender("user1")
                .type("text")
                .data("hi")
                .createdAt(LocalDateTime.now())
                .build();

        Message message2 = Message.builder()
                .channelId(groupId)
                .sender("user2")
                .type("text")
                .data("hello")
                .createdAt(LocalDateTime.now())
                .build();

        Page<Message> messagePage = new PageImpl<>(List.of(message1, message2), pageable, 2);

        MessageResponseDto.MessageDto dto1 = MessageResponseDto.MessageDto.builder()
                .sender("user1")
                .channelId(groupId)
                .data("hi")
                .createdAt(LocalDateTime.now())
                .build();

        MessageResponseDto.MessageDto dto2 = MessageResponseDto.MessageDto.builder()
                .sender("user2")
                .channelId(groupId)
                .data("hi")
                .createdAt(LocalDateTime.now())
                .build();

        MessageResponseDto.SearchMessage response = MessageResponseDto.SearchMessage.builder()
                .messageResDtos(List.of(dto1,dto2))
                .isFirst(true)
                .isLast(true)
                .listSize(2)
                .totalElements(2)
                .build();

        when(messageRepository.findByChannelId(groupId, pageable)).thenReturn(messagePage);

        try (MockedStatic<MessageConverter> mockedStatic = mockStatic(MessageConverter.class)) {
            mockedStatic.when(() -> MessageConverter.toSearchMessage(messagePage))
                    .thenReturn(response);

            // when
            MessageResponseDto.SearchMessage result = messageService.getMessages(groupId, page, size);

            // then
            assertNotNull(result);
            assertEquals(response, result);

            verify(messageRepository, times(1)).findByChannelId(groupId, pageable);
        }

    }


}
