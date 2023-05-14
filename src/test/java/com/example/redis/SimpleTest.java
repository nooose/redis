package com.example.redis;

import com.example.redis.service.RankingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
public class SimpleTest {

    @Autowired
    private RankingService rankingService;

    @Test
    void getRanks() {
        StopWatch userRankStopWatch = new StopWatch();
        userRankStopWatch.start();
        Long userRank = rankingService.getUserRanking("user100");
        userRankStopWatch.stop();

        StopWatch topRankingStopWatch = new StopWatch();
        topRankingStopWatch.start();
        List<String> topRankUsers = rankingService.getTopRank(10);
        topRankingStopWatch.stop();

        System.out.println("Rank: " + userRank);
        System.out.println(userRankStopWatch.getTotalTimeMillis() + "ms");

        System.out.println("Ranking: " + topRankUsers);
        System.out.println(topRankingStopWatch.getTotalTimeMillis() + "ms");
    }

    @Test
    void insertScore() {
        IntStream.range(0, 1000000)
                .forEach(i -> {
                    int score = (int) (Math.random() * 1000000);
                    String userId = "user" + i;
                    rankingService.setUserScore(userId, score);
                });
    }

    @Test
    void inMemorySortPerformanceTest() {
        final List<Integer> scores = new ArrayList<>();
        IntStream.range(0, 1000000)
                .forEach(i -> {
                    int score = (int) (Math.random() * 1000000);
                    scores.add(score);
                });

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Collections.sort(scores);
        stopWatch.stop();

        System.out.println(stopWatch.getTotalTimeMillis() + "ms");
    }
}
