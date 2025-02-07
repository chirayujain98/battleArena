package com.gocomet.battleArena.controller;

import com.gocomet.battleArena.dto.ScoreSubmissionRequest;
import com.gocomet.battleArena.entity.GameSession;
import com.gocomet.battleArena.entity.Leaderboard;
import com.gocomet.battleArena.service.LeaderboardService;
import com.newrelic.api.agent.NewRelic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {
    @Autowired
    private LeaderboardService leaderboardService;

    @PostMapping("/submit")
    public ResponseEntity<String> submitScore(@RequestBody ScoreSubmissionRequest request) {
        long start = System.currentTimeMillis();
        leaderboardService.submitScore(request.getUserId(), request.getScore(), request.getGameMode());
        long duration = System.currentTimeMillis() - start;
        NewRelic.recordMetric("Custom/Leaderboard/SubmitScore", duration);
        return ResponseEntity.ok("Score submitted successfully!");
    }

    @GetMapping("/top")
    public ResponseEntity<List<Leaderboard>> getLeaderboard() {
        long start = System.currentTimeMillis();
        List<Leaderboard> response = leaderboardService.getTopPlayers();
        long duration = System.currentTimeMillis() - start;
        try {
            NewRelic.recordMetric("Custom/Leaderboard/Top", duration);
        } catch (Exception e) {
            System.err.println("New Relic recording failed: " + e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rank/{userId}")
    public ResponseEntity<Integer> getPlayerRank(@PathVariable int userId) {
        return ResponseEntity.ok(leaderboardService.getPlayerRank(userId));
    }
}
