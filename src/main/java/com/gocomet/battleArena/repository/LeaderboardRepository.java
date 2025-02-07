package com.gocomet.battleArena.repository;

import com.gocomet.battleArena.entity.Leaderboard;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, Integer> {
    @Query("SELECT l FROM Leaderboard l ORDER BY l.totalScore DESC")
    List<Leaderboard> findTopPlayers(Pageable pageable);

    @Query("SELECT COUNT(l) + 1 FROM Leaderboard l WHERE l.totalScore > (SELECT l2.totalScore FROM Leaderboard l2 WHERE l2.user.id = :userId)")
    Integer findRankByUserId(@Param("userId") int userId);

    @Modifying
    @Query(value = "INSERT INTO leaderboard (user_id, total_score, `rank`) VALUES (:userId, :totalScore, :rank) " +
            "ON DUPLICATE KEY UPDATE total_score = :totalScore, `rank` = :rank", nativeQuery = true)
    @Transactional
    void saveCustom(@Param("userId") int userId,
                    @Param("totalScore") int totalScore,
                    @Param("rank") Integer rank);

    @Query("SELECT COUNT(l) + 1 FROM Leaderboard l WHERE l.totalScore > :totalScore")
    Integer findRankForScore(@Param("totalScore") int totalScore);


}

