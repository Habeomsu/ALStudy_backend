package main.als.websocket.service;

import jakarta.transaction.Transactional;
import main.als.websocket.entity.Message;
import main.als.websocket.repository.MessageRepository;


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
}
