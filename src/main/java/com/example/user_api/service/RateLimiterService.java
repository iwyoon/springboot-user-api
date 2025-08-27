package com.example.user_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisTemplate<String, Long> redisTemplate;

    /**
     * Rate limit 체크
     * @param key Redis key (ex: "kakao:202508261430")
     * @param limit 최대 호출 가능 횟수
     * @param ttlSeconds TTL (초 단위, 1분 = 60초)
     * @return true: 제한 내, false: 초과
     */
    public boolean tryAcquire(String key, long limit, long ttlSeconds) {
        Long count = redisTemplate.opsForValue().increment(key); // 1 증가
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
        }

        return count <= limit;
    }

    /**
     * 특정 key의 현재 카운트 조회
     *
     * @param key Redis key
     * @return 현재 호출 횟수
     */
    public long getCount(String key) {
        Object count = redisTemplate.opsForValue().get(key);
        if (count instanceof Number) {
            return ((Number) count).longValue();
        }
        return 0L;
    }

}