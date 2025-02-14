package main.als.websocket.repository;

import main.als.websocket.entity.Message;
import org.springframework.data.repository.CrudRepository;


public interface MessageRepository extends CrudRepository<Message, Long> {

    Message save(Message message);
}
