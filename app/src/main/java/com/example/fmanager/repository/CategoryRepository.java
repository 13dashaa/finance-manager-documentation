package com.example.fmanager.repository;

import com.example.fmanager.models.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    @Query(value = """
            SELECT DISTINCT categories.id
            FROM clients
            JOIN accounts ON clients.id = accounts.client_id
            JOIN transactions ON accounts.id = transactions.account_id
            JOIN categories ON categories.id = transactions.category_id
            WHERE clients.id = :clientId
            """, nativeQuery = true)
    List<Integer> findCategoryIdsByClientId(@Param("clientId") int clientId);
}

