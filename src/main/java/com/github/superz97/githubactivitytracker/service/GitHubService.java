package com.github.superz97.githubactivitytracker.service;

import com.github.superz97.githubactivitytracker.model.GitHubEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class GitHubService {

    private final WebClient.Builder webClientBuilder;

    @Value("${github.api.base-url:https://api.github.com}")
    private String githubApiBaseUrl;

    @Value("${github.api.token:}")
    private String githubToken;

    public Flux<GitHubEvent> getUserActivity(String username) {
        WebClient webClient = createWebClient();
        return webClient.get()
                .uri("/users/{username}/events/public", username)
                .retrieve()
                .bodyToFlux(GitHubEvent.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(throwable -> throwable instanceof WebClientResponseException &&
                                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                .onErrorResume(WebClientResponseException.NotFound.class, e -> {
                    log.error("User not found: {}", username);
                    return Flux.error(new RuntimeException("User '" + username + "' not found on GitHub"));
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("GitHub API error: {} - {}", e.getStatusCode(), e.getMessage());
                    return Flux.error(new RuntimeException("GitHub API error: " + e.getMessage()));
                })
                .doOnNext(event -> log.debug("Fetched event: {} for user: {}", event.getType(), username))
                .doOnError(error -> log.error("Error fetching activity for user: {}", username, error));
    }

    public Mono<Boolean> validateUser(String username) {
        WebClient webClient = createWebClient();
        return webClient.get()
                .uri("/users/{username}", username)
                .retrieve()
                .toBodilessEntity()
                .map(response -> true)
                .onErrorReturn(WebClientResponseException.NotFound.class, false)
                .onErrorReturn(false);
    }

    private WebClient createWebClient() {
        return webClientBuilder
                .baseUrl(githubApiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeaders(headers -> {
                    if (!githubToken.isEmpty()) {
                        headers.setBearerAuth(githubToken);
                    }
                })
                .build();

    }

}
