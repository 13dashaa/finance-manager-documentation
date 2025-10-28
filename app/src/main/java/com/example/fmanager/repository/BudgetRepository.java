package com.example.fmanager.repository;

import com.example.fmanager.models.Budget;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {
    @Query("SELECT b FROM Budget b "
            + "JOIN b.clients c "
            + "WHERE b.category.id = :categoryId AND c.id = :clientId")
    List<Budget> findByCategoryIdAndClientId(@Param("categoryId") int categoryId,
                                             @Param("clientId") int clientId);
}

