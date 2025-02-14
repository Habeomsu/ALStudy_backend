package main.als.websocket.service;

import jakarta.transaction.Transactional;
import main.als.websocket.converter.MessageConverter;
import main.als.websocket.dto.MessageResponseDto;
import main.als.websocket.entity.Message;
import main.als.websocket.repository.MessageRepository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional
    public Message createMessage(Message message) {

        Message newMessage = messageRepository.save(message);

        return newMessage;
    }

    @Override
    public MessageResponseDto.SearchMessage getMessages(String groupId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findByChannelId(groupId, pageable);

        return MessageConverter.toSearchMessage(messages);
    }
}
