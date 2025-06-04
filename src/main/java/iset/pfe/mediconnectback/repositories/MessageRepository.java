package iset.pfe.mediconnectback.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.Message;
import iset.pfe.mediconnectback.entities.User;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
   List<Message> findByConversationIdOrderByDateCreatedAsc(Long conversationId);

   List<Message> findByConversationId(Long conversationId);

   List<Message> findByConversationIdAndContentContainingIgnoreCase(Long conversationId, String content);

    @Query("SELECT m FROM Message m WHERE " +
       "m.content LIKE %:search%")
    List<Message> findByContent(@Param("search") String search);



}
