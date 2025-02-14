package main.als.websocket.repository;

import main.als.websocket.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


public interface MessageRepository extends CrudRepository<Message, Long> {

    Message save(Message message);

    @Query("SELECT m FROM Message m WHERE m.channelId = :channelId ORDER BY m.createdAt DESC")
    Page<Message> findByChannelId(String channelId, Pageable pageable);
}
