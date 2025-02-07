package com.gocomet.battleArena.repository;

import com.gocomet.battleArena.entity.Leaderboard;
import com.newrelic.api.agent.Trace;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, Integer> {
    @Trace
    @Query("SELECT l FROM Leaderboard l ORDER BY l.totalScore DESC")
    List<Leaderboard> findTopPlayers(Pageable pageable);

    @Query("SELECT COUNT(l) + 1 FROM Leaderboard l WHERE l.totalScore > (SELECT l2.totalScore FROM Leaderboard l2 WHERE l2.user.id = :userId)")
    Integer findRankByUserId(@Param("userId") int userId);

    @Modifying
    @Query(value = "INSERT INTO leaderboard (user_id, total_score, `rank`) VALUES (:userId, :totalScore, :rank) " +
            "ON DUPLICATE KEY UPDATE total_score = :totalScore, `rank` = :rank", nativeQuery = true)
    @Transactional
    @Trace
    void saveCustom(@Param("userId") int userId,
                    @Param("totalScore") int totalScore,
                    @Param("rank") Integer rank);

    @Query("SELECT COUNT(l) + 1 FROM Leaderboard l WHERE l.totalScore > :totalScore")
    Integer findRankForScore(@Param("totalScore") int totalScore);

    /*@Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Leaderboard l WHERE l.userId = :userId")
    Leaderboard findByUserIdForUpdate(Long userId);*/

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE leaderboard l
        JOIN (
            SELECT user_id, 
                   RANK() OVER (ORDER BY total_score DESC) AS new_rank
            FROM leaderboard
        ) r ON l.user_id = r.user_id
        SET l.`rank` = r.new_rank
    """, nativeQuery = true)
    void updateRanks();


    @Query(value = "SELECT total_score FROM leaderboard WHERE user_id = :userId", nativeQuery = true)
    Integer findTotalScoreByUserId(int userId);

    List<Leaderboard> findTop10ByOrderByTotalScoreDesc();

    Optional<Leaderboard> findByUserId(Integer userId);
}

