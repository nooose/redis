package com.example.redis.service;

import com.example.redis.service.dto.UserProfile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    private final ExternalApiService externalApiService;
    private final StringRedisTemplate redisTemplate;

    public UserService(ExternalApiService externalApiService, StringRedisTemplate redisTemplate) {
        this.externalApiService = externalApiService;
        this.redisTemplate = redisTemplate;
    }

    public UserProfile getUserProfile(String userId) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String cachedName = ops.get("nameKey:" + userId);
        String userName;
        if (cachedName != null) {
            userName = cachedName;
        } else {
            userName = externalApiService.getUserName(userId);
            ops.set("nameKey:" + userId, userName, 5, TimeUnit.SECONDS);
        }

        int userAge = externalApiService.getUserAge(userId);
        return new UserProfile(userName, userAge);
    }
}
