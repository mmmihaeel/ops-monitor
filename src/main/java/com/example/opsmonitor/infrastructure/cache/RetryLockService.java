package com.example.opsmonitor.infrastructure.cache;

import java.time.Duration;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RetryLockService {

  private static final String LOCK_PREFIX = "ops-monitor:retry-lock:";

  private final StringRedisTemplate redisTemplate;

  public RetryLockService(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public boolean acquire(UUID failedJobId, Duration ttl) {
    String key = LOCK_PREFIX + failedJobId;
    Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
    return Boolean.TRUE.equals(acquired);
  }

  public void release(UUID failedJobId) {
    redisTemplate.delete(LOCK_PREFIX + failedJobId);
  }
}
