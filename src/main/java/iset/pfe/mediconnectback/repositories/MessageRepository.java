package iset.pfe.mediconnectback.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.Message;

@Repository
public interface MessageRepository {
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :senderId AND m.receiver.id = :receiverId) OR " +
           "(m.sender.id = :receiverId AND m.receiver.id = :senderId) " +
           "AND m.isDeleted = false " +
           "ORDER BY m.dateCreated ASC")
    List<Message> findMessagesBetween(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}
