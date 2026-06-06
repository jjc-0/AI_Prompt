package com.ecommerce.agent.repository;

import com.ecommerce.agent.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByEnabledTrueOrderByNameAsc();

    List<Product> findByCategoryAndEnabledTrue(String category);

    List<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(String name, String sku);

    long countByEnabledTrue();
}
