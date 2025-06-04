package iset.pfe.mediconnectback.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import iset.pfe.mediconnectback.entities.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c JOIN c.participants p1 JOIN c.participants p2 " +
           "WHERE p1.id = :user1Id AND p2.id = :user2Id AND SIZE(c.participants) = 2 AND c.type = 'PRIVATE'")
    Optional<Conversation> findPrivateConversationBetween(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    List<Conversation> findAllByParticipantsId(Long userId);
    

}
