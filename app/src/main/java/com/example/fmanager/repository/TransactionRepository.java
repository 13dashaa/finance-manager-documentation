package com.example.fmanager.repository;

import com.example.fmanager.models.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    @Query("SELECT t FROM Transaction t "
            + "JOIN t.account a "
            + "JOIN a.client cl "
            + "JOIN t.category ct "
            + "WHERE cl.id = :clientId "
            + "AND ct.id = :categoryId")
    List<Transaction> findAllByClientIdAndCategoryId(@Param("clientId") int clientId,
                                                     @Param("categoryId") int categoryId);
}


