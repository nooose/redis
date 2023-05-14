package com.example.redis.controller.cache;

import com.example.redis.service.RankingService;
import com.example.redis.service.UserService;
import com.example.redis.service.dto.UserProfile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ApiController {

    private final UserService userService;
    private final RankingService rankingService;

    public ApiController(UserService userService, RankingService rankingService) {
        this.userService = userService;
        this.rankingService = rankingService;
    }

    @GetMapping("/users/{userId}/profile")
    public UserProfile getUserProfile(@PathVariable String userId) {
        return userService.getUserProfile(userId);
    }

    @GetMapping("/setScore")
    public Boolean setScore(@RequestParam String userId, @RequestParam int score) {
        return rankingService.setUserScore(userId, score);
    }

    @GetMapping("/getRank")
    public Long setScore(@RequestParam String userId) {
        return rankingService.getUserRanking(userId);
    }

    @GetMapping("/getTopRank")
    public List<String> getTopRanks() {
        return rankingService.getTopRank(3);
    }
}
