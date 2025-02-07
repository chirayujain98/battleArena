


INSERT INTO leaderboard (user_id, total_score, `rank`)
SELECT user_id, total_score, user_rank
FROM (
    SELECT
        user_id,
        SUM(score) AS total_score,
        RANK() OVER (ORDER BY SUM(score) DESC) AS user_rank
    FROM game_sessions
    GROUP BY user_id
) AS ranked_users;
