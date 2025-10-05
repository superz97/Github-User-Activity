package com.github.superz97.githubactivitytracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.superz97.githubactivitytracker.entity.ActivityRecord;
import com.github.superz97.githubactivitytracker.model.GitHubEvent;
import com.github.superz97.githubactivitytracker.repository.ActivityRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final GitHubService gitHubService;
    private final CacheService cacheService;
    private final ActivityRecordRepository activityRepository;
    private final ObjectMapper objectMapper;

    public Flux<GitHubEvent> getUserActivity(String username, boolean forceRefresh) {
        if (forceRefresh) {
            return fetchAndCacheActivity(username);
        }
        return cacheService.getCachedActivity(username)
                .flatMapMany(cachedEvents -> {
                    if (cachedEvents != null && !cachedEvents.isEmpty()) {
                        return Flux.fromIterable(cachedEvents);
                    }
                    return fetchAndCacheActivity(username);
                })
                .switchIfEmpty(fetchAndCacheActivity(username));
    }

    private Flux<GitHubEvent> fetchAndCacheActivity(String username) {
        return gitHubService.getUserActivity(username)
                .collectList()
                .flatMapMany(events -> {
                    cacheService.cacheActivity(username, events).subscribe();
                    saveToDatabase(username, events).subscribe();
                    return Flux.fromIterable(events);
                });
    }

    // Mono<Void>
    private Mono<Void> saveToDatabase(String username, List<GitHubEvent> events) {
        return Mono.fromRunnable(() -> {
                    try {
                        List<ActivityRecord> records = events.stream()
                                .map(event -> {
                                    ActivityRecord record = new ActivityRecord();
                                    record.setEventId(event.getId());
                                    record.setUsername(username);
                                    record.setEventType(event.getType());
                                    record.setRepositoryName(event.getRepo().getName());

                                    try {
                                        record.setDescription(event.getFormattedActivity());
                                    } catch (Exception e) {
                                        log.warn("Error formatting activity for event {}: {}", event.getId(), e.getMessage());
                                        record.setDescription(event.getType() + " on " + event.getRepo().getName());
                                    }

                                    record.setEventTime(event.getCreatedAt());
                                    record.setFetchedAt(LocalDateTime.now());

                                    try {
                                        record.setRawPayload(objectMapper.writeValueAsString(event.getPayload()));
                                    } catch (Exception e) {
                                        log.error("Error serializing payload for event: {}", event.getId(), e);
                                        record.setRawPayload("{}");
                                    }

                                    return record;
                                })
                                .collect(Collectors.toList());

                        activityRepository.saveAll(records);
                        log.info("Saved {} activity records for user: {}", records.size(), username);
                    } catch (Exception e) {
                        log.error("Error saving activities to database for user: {}", username, e);
                    }
                }).subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(throwable -> {
                    log.error("Database save failed for user: {}", username, throwable);
                    return Mono.empty();
                }).then();
    }

    public Flux<GitHubEvent> getFilteredActivity(String username, String eventType) {
        return getUserActivity(username, false)
                .filter(event -> event.getType().equalsIgnoreCase(eventType));
    }

    public Mono<List<ActivityRecord>> getHistoricalActivity(String username) {
        return Mono.fromCallable(() -> activityRepository.findByUsernameOrderByEventTimeDesc(username))
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<String>> getAvailableEventTypes(String username) {
        return Mono.fromCallable(() -> activityRepository.findDistinctEventTypeByUsername(username))
                .subscribeOn(Schedulers.boundedElastic());
    }

}
