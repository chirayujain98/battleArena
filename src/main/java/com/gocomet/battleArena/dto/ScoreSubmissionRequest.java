package com.gocomet.battleArena.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScoreSubmissionRequest {
    @JsonProperty("user_id")
    private int userId;
    @JsonProperty("score")
    private int score;
    @JsonProperty("game_mode")
    private String gameMode;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }
}
