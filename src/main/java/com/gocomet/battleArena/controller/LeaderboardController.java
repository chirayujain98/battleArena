package com.gocomet.battleArena.controller;

import com.gocomet.battleArena.dto.ScoreSubmissionRequest;
import com.gocomet.battleArena.entity.GameSession;
import com.gocomet.battleArena.entity.Leaderboard;
import com.gocomet.battleArena.service.LeaderboardService;
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
        leaderboardService.submitScore(request.getUserId(), request.getScore(), request.getGameMode());
        return ResponseEntity.ok("Score submitted successfully!");
    }

    @GetMapping("/top")
    public ResponseEntity<List<Leaderboard>> getLeaderboard() {
        return ResponseEntity.ok(leaderboardService.getTopPlayers());
    }

    @GetMapping("/rank/{userId}")
    public ResponseEntity<Integer> getPlayerRank(@PathVariable int userId) {
        return ResponseEntity.ok(leaderboardService.getPlayerRank(userId));
    }
}
