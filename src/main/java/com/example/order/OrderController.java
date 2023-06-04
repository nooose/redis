package com.example.order;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class OrderController {

    private final StringRedisTemplate redisTemplate;

    public OrderController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/order")
    public String order(
            @RequestParam String userId,
            @RequestParam String produceId,
            @RequestParam String price
    ) {

        Map<String, String> field = Map.of("userId", userId,
                "produceId", produceId,
                "price", price);

        redisTemplate.opsForStream().add("order-event", field);
        return "OK";
    }
}
