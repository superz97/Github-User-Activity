package com.github.superz97.githubactivitytracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_records")
@Data
public class ActivityRecord {

    @Id
    private String eventId;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String repositoryName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventTime;

    @Column(nullable = false)
    private LocalDateTime fetchedAt;

    @Column(columnDefinition = "TEXT")
    private String rawPayload;

}
