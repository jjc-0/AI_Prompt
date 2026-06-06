package com.ecommerce.agent.repository;

import com.ecommerce.agent.model.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    List<KnowledgeDocument> findByEnabledTrueOrderByTitleAsc();

    List<KnowledgeDocument> findByCategoryAndEnabledTrue(String category);

    List<KnowledgeDocument> findByTitleContainingIgnoreCase(String keyword);
}
