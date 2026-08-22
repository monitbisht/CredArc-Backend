package com.credarc.credarc.redis;

import com.credarc.credarc.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterServiceTest {

    @InjectMocks
    private RateLimiterService rateLimiterService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void checkRateLimit_setsExpiry_onFirstIncrement() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("key", 1)).thenReturn(1L);

        rateLimiterService.checkRateLimit("key", 5);

        verify(redisTemplate).expire("key", Duration.ofMinutes(1));
    }

    @Test
    void checkRateLimit_doesNotResetExpiry_whenTtlStillValid() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("key", 1)).thenReturn(3L);
        when(redisTemplate.getExpire("key")).thenReturn(45L);

        rateLimiterService.checkRateLimit("key", 5);

        verify(redisTemplate, never()).expire(eq("key"), any(Duration.class));
    }

    @Test
    void checkRateLimit_resetsExpiry_whenTtlLeaked() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("key", 1)).thenReturn(3L);
        when(redisTemplate.getExpire("key")).thenReturn(-1L);

        rateLimiterService.checkRateLimit("key", 5);

        verify(redisTemplate).expire("key", Duration.ofMinutes(1));
    }

    @Test
    void checkRateLimit_throws_whenCountExceedsMax() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("key", 1)).thenReturn(6L);
        when(redisTemplate.getExpire("key")).thenReturn(30L);

        assertThrows(RateLimitExceededException.class,
                () -> rateLimiterService.checkRateLimit("key", 5));
    }

    @Test
    void checkRateLimit_doesNotThrow_whenCountWithinLimit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("key", 1)).thenReturn(5L);
        when(redisTemplate.getExpire("key")).thenReturn(30L);

        assertDoesNotThrow(() -> rateLimiterService.checkRateLimit("key", 5));
    }
}