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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.util.List;

@Service
public class LeaderboardService {
    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private LeaderboardRepository leaderboardRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void submitScore(int userId, int score, String gameMode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        GameSession session = new GameSession();
        session.setUser(user);
        session.setScore(score);
        session.setGameMode(gameMode);
        gameSessionRepository.save(session);

        Integer totalScore = gameSessionRepository.findTotalScoreByUserId(userId);
        Leaderboard leaderboard = leaderboardRepository.findById(userId)
                .orElse(new Leaderboard());
        leaderboard.setUser(user);
        leaderboard.setTotalScore(totalScore);
        int rank= leaderboardRepository.findRankForScore(totalScore);
        leaderboardRepository.saveCustom(userId, totalScore, rank);
    }

    public List<Leaderboard> getTopPlayers() {
        return leaderboardRepository.findTopPlayers( PageRequest.of(0, 10));
    }



    public Integer getPlayerRank(int userId) {
        return leaderboardRepository.findRankByUserId(userId);
    }
}

