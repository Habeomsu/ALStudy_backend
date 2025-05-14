package main.als.websSocketTest;

import main.als.websocket.entity.Message;
import main.als.websocket.repository.MessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@ActiveProfiles("test")
public class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    LocalDateTime now = LocalDateTime.now();

    @Test
    @DisplayName("findByChannelId 테스트")
    public void findByChannelIdTest() {

        String channelId = "channelId";

        Message message1 = Message.builder()
                .channelId(channelId)
                .sender("sender")
                .type("type")
                .data("data")
                .createdAt(now)
                .build();

        Message message2 = Message.builder()
                .channelId(channelId)
                .sender("sender")
                .type("type")
                .data("data")
                .createdAt(now)
                .build();

        Message message3 = Message.builder()
                .channelId(channelId)
                .sender("sender")
                .type("type")
                .data("data")
                .createdAt(now)
                .build();

        messageRepository.saveAll(List.of(message1, message2, message3));

        Pageable pageable = PageRequest.of(0, 2);

        Page<Message> page = messageRepository.findByChannelId(channelId, pageable);

        assertEquals(2, page.getContent().size());
        assertEquals(channelId, page.getContent().get(0).getChannelId()); // 최신 메시지가 먼저
        assertEquals(channelId, page.getContent().get(1).getChannelId());



    }


}
