package com.ecommerce.agent.repository;

import com.ecommerce.agent.model.ConversationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationSessionRepository extends JpaRepository<ConversationSession, Long> {

    Optional<ConversationSession> findBySessionId(String sessionId);

    List<ConversationSession> findAllByOrderByUpdatedAtDesc();

    List<ConversationSession> findByOperationTypeOrderByUpdatedAtDesc(String operationType);

    void deleteBySessionId(String sessionId);
}
