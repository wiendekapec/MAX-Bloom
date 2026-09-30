package service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис ограничения частоты запросов (Rate Limiting) на базе Bucket4j.
 */
@Slf4j
@Service
public class RateLimitService {

    private static final int TOKENS_PER_MINUTE = 60;
    private static final int BURST_CAPACITY = 20;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Проверка и списание токена для стандартных запросов.
     */
    public boolean tryConsume(String key) {
        Bucket bucket = buckets.computeIfAbsent(key, this::newBucket);
        return bucket.tryConsume(1);
    }

    /**
     * Проверка и списание токена для платежных операций.
     */
    public boolean tryConsumePayment(String key) {
        Bucket bucket = buckets.computeIfAbsent("pay:" + key, this::newPaymentBucket);
        return bucket.tryConsume(1);
    }

    private Bucket newBucket(String key) {
        Bandwidth limit = Bandwidth.classic(
                TOKENS_PER_MINUTE,
                Refill.greedy(TOKENS_PER_MINUTE, Duration.ofMinutes(1))
        ).withInitialTokens(BURST_CAPACITY);
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket newPaymentBucket(String key) {
        Bandwidth limit = Bandwidth.classic(
                5,
                Refill.greedy(5, Duration.ofMinutes(1))
        ).withInitialTokens(3);
        return Bucket.builder().addLimit(limit).build();
    }
}
