package com.gocomet.battleArena.repository;

import com.gocomet.battleArena.entity.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Integer> {
    @Query("SELECT SUM(gs.score) FROM GameSession gs WHERE gs.user.id = :userId")
    Integer findTotalScoreByUserId(@Param("userId") int userId);
}
