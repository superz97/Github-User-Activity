package com.github.superz97.githubactivitytracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableJpaRepositories
@EnableScheduling
public class GitHubActivityTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitHubActivityTrackerApplication.class, args);
    }

}
