package com.ecommerce.agent.repository;

import com.ecommerce.agent.model.ClawChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClawChannelRepository extends JpaRepository<ClawChannel, Long> {

    List<ClawChannel> findByEnabledTrueOrderByCreatedAtDesc();

    Optional<ClawChannel> findByBindingKey(String bindingKey);

    Optional<ClawChannel> findByTypeAndEnabledTrue(String type);

    long countByEnabledTrue();
}
