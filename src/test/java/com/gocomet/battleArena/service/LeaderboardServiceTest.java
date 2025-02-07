package com.gocomet.battleArena.service;

import com.gocomet.battleArena.entity.GameSession;
import com.gocomet.battleArena.entity.Leaderboard;
import com.gocomet.battleArena.entity.User;
import com.gocomet.battleArena.exceptions.UserNotFoundException;
import com.gocomet.battleArena.repository.GameSessionRepository;
import com.gocomet.battleArena.repository.LeaderboardRepository;
import com.gocomet.battleArena.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private GameSessionRepository gameSessionRepository;

    @Mock
    private LeaderboardRepository leaderboardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ZSetOperations<String, String> redisZSet;

    @InjectMocks
    private LeaderboardService leaderboardService;

    private User user;
    private Leaderboard leaderboard;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("Player1");

        leaderboard = new Leaderboard();
        leaderboard.setUser(user);
        leaderboard.setTotalScore(100);
    }

    @Test
    void submitScoreUserNotFoundShouldThrowException() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> leaderboardService.submitScore(1, 50, "Battle"));
    }

    @Test
    void submitScoreValidUserShouldUpdateLeaderboard() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(gameSessionRepository.findTotalScoreByUserId(1)).thenReturn(150);
        when(redisZSet.reverseRank("leaderboard", "1")).thenReturn(2L);

        leaderboardService.submitScore(1, 50, "Battle");

        verify(gameSessionRepository).save(any(GameSession.class));
        verify(redisZSet).add("leaderboard", "1", 150);
        verify(leaderboardRepository).saveCustom(1, 150, 3);
        verify(leaderboardRepository).updateRanks();
    }

    @Test
    void getTopPlayersWhenRedisHasDataShouldReturnFromRedis() {
        when(redisZSet.reverseRange("leaderboard", 0, 9)).thenReturn(Set.of("1"));
        when(leaderboardRepository.findByUserId(1)).thenReturn(Optional.of(leaderboard));

        List<Leaderboard> topPlayers = leaderboardService.getTopPlayers();
        assertFalse(topPlayers.isEmpty());
        assertEquals(1, topPlayers.size());
        assertEquals(100, topPlayers.get(0).getTotalScore());
    }

    @Test
    void getPlayerRankWhenUserExistsShouldReturnRank() {
        when(redisZSet.reverseRank("leaderboard", "1")).thenReturn(3L);
        Integer rank = leaderboardService.getPlayerRank(1);
        assertEquals(4, rank);
    }

    @Test
    void getPlayerRankWhenNotInRedisShouldFetchFromDatabase() {
        when(redisZSet.reverseRank("leaderboard", "1")).thenReturn(null);
        when(leaderboardRepository.findTotalScoreByUserId(1)).thenReturn(100);
        when(leaderboardRepository.findRankForScore(100)).thenReturn(5);

        Integer rank = leaderboardService.getPlayerRank(1);
        assertEquals(5, rank);
    }
}
