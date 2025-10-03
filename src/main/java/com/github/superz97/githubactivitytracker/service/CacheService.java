package com.github.superz97.githubactivitytracker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.superz97.githubactivitytracker.model.GitHubEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String CACHE_KEY_PREFIX = "github:activity:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    public Mono<List<GitHubEvent>> getCachedActivity(String username) {
        String key = CACHE_KEY_PREFIX + username;
        return redisTemplate.opsForValue()
                .get(key)
                .mapNotNull(json -> {
                    try {
                        return objectMapper.readValue(json, new TypeReference<List<GitHubEvent>>() {});
                    } catch (Exception e) {
                        log.error("Error deserializing cached data for user: {}", username, e);
                        return null;
                    }
                })
                .doOnNext(events -> {
                    if (events != null) {
                        log.info("Cache hit for user: {}, found {} events", username, events.size());
                    }
                });
    }

    public Mono<Boolean> cacheActivity(String username, List<GitHubEvent> events) {
        String key = CACHE_KEY_PREFIX + username;
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(events))
                .flatMap(json -> redisTemplate.opsForValue().set(key, json, CACHE_TTL))
                .doOnSuccess(result -> log.info("Cached {} events for user: {}", events.size(), username))
                .onErrorResume(e -> {
                    log.error("Error caching data for user: {}", username, e);
                    return Mono.just(false);
                });
    }

    public Mono<Boolean> invalidateCache(String username) {
        String key = CACHE_KEY_PREFIX + username;
        return redisTemplate.delete(key)
                .map(count -> count > 0)
                .doOnSuccess(result -> {
                    if (result) {
                        log.info("Cache invalidated for user: {}",  username);
                    }
                });
    }

}
