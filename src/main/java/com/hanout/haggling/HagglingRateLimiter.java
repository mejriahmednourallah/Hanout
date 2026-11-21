package com.hanout.haggling;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class HagglingRateLimiter {

    private final Map<Long, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket createNewBucket() {
        // Allow up to 10 offer actions per minute per buyer
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillGreedy(10, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    public boolean tryConsume(Long buyerId) {
        Bucket bucket = buckets.computeIfAbsent(buyerId, k -> createNewBucket());
        return bucket.tryConsume(1);
    }
}
