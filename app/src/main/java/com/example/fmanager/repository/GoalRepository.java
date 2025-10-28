package com.example.fmanager.repository;

import com.example.fmanager.models.Goal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Integer> {
    @Query("SELECT g FROM Goal g JOIN g.client c WHERE c.id = :clientId")
    List<Goal> findByClientId(@Param("clientId") int clientId);
}
