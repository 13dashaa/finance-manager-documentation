package com.example.fmanager.repository;

import com.example.fmanager.models.Account;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    @Query("SELECT a FROM Account a JOIN a.client c WHERE c.id = :clientId")
    List<Account> findAllByClientId(@Param("clientId") int clientId);

    @Query("SELECT a FROM Account a WHERE a.client.username = :clientUsername")
    List<Account> findByClientUsername(String clientUsername);

}
