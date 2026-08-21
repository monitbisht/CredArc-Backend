package com.credarc.credarc.redis;


import com.credarc.credarc.exception.RateLimitExceededException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void checkRateLimit(String key, int maxAttempts) {
        Long count = redisTemplate.opsForValue().increment(key, 1);
        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        } else if (count != null) {
            Long ttl = redisTemplate.getExpire(key);
            if (ttl != null && ttl < 0) {
                redisTemplate.expire(key, Duration.ofMinutes(1));
            }
        }
        if (count != null && count > maxAttempts) {
            throw new RateLimitExceededException("Too many attempts. Try again later.");
        }
    }
}
