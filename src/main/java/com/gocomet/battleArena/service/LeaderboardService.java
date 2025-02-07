package com.gocomet.battleArena.service;

import com.gocomet.battleArena.entity.GameSession;
import com.gocomet.battleArena.entity.Leaderboard;
import com.gocomet.battleArena.entity.User;
import com.gocomet.battleArena.exceptions.UserNotFoundException;
import com.gocomet.battleArena.repository.GameSessionRepository;
import com.gocomet.battleArena.repository.LeaderboardRepository;
import com.gocomet.battleArena.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {
    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private LeaderboardRepository leaderboardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ZSetOperations<String, String> redisZSet;

    private static final String LEADERBOARD_KEY = "leaderboard";

    @Transactional
    public void submitScore(int userId, int score, String gameMode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        GameSession session = new GameSession();
        session.setUser(user);
        session.setScore(score);
        session.setGameMode(gameMode);
        gameSessionRepository.save(session);

        // Step 1: Compute Total Score for User
        Integer totalScore = gameSessionRepository.findTotalScoreByUserId(userId);

        // Step 2: Update Redis Sorted Set (Efficiently handles ranking)
        redisZSet.add(LEADERBOARD_KEY, String.valueOf(userId), totalScore);

        Long redisRank = redisZSet.reverseRank(LEADERBOARD_KEY, String.valueOf(userId));

        int userRank;
        if (redisRank != null) {
            userRank = redisRank.intValue() + 1; // Convert 0-based to 1-based rank
        } else {
            // Step 5: If Redis is empty, fetch from database
            userRank = leaderboardRepository.findRankForScore(totalScore);
        }
        // Step 3: Update the Leaderboard Table (Database)

        leaderboardRepository.saveCustom(userId,totalScore,userRank);
        leaderboardRepository.updateRanks();

    }

    @Cacheable(value = "leaderboardTop10")
    public List<Leaderboard> getTopPlayers() {
        Set<String> topUsers = redisZSet.reverseRange(LEADERBOARD_KEY, 0, 9);

        if (topUsers != null && !topUsers.isEmpty()) {
            // Fetch from database and return
            return topUsers.stream()
                    .map(userId -> {
                        return leaderboardRepository.findByUserId(Integer.parseInt(userId)).orElse(null);
                    })
                    .collect(Collectors.toList());
        }

        // Step 2: If Redis is empty, fallback to database
        List<Leaderboard> topPlayers = leaderboardRepository.findTop10ByOrderByTotalScoreDesc();

        // Step 3: Store fetched data in Redis for future queries
        topPlayers.forEach(player ->
                redisZSet.add(LEADERBOARD_KEY, String.valueOf(player.getUser().getId()), player.getTotalScore())
        );

        return topPlayers;
    }

    @Transactional
    @CacheEvict(value = "leaderboardTop10", allEntries = true) // Invalidate cache on update
    public void updateLeaderboard(int userId, int newScore) {
        Leaderboard leaderboard = leaderboardRepository.findById(userId)
                .orElse(new Leaderboard());

        // Update Total Score
        int updatedScore = leaderboard.getTotalScore() + newScore;
        leaderboard.setTotalScore(updatedScore);
        leaderboardRepository.save(leaderboard);

        // Update Redis for fast ranking updates
        redisZSet.add(LEADERBOARD_KEY, String.valueOf(userId), updatedScore);
    }

    public Integer getPlayerRank(int userId) {
        Long rank = redisZSet.reverseRank(LEADERBOARD_KEY, String.valueOf(userId));

        if (rank != null && rank != 0) {
            return rank.intValue() + 1; // Convert 0-based to 1-based rank
        }

        // Step 2: If Redis is empty, fetch from the database
        Integer totalScore = leaderboardRepository.findTotalScoreByUserId(userId);
        if (totalScore == null) {
            return null; // User not found in leaderboard
        }

        return leaderboardRepository.findRankForScore(totalScore); // Convert 0-based to 1-based rank
    }
}

