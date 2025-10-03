package com.github.superz97.githubactivitytracker.repository;

import com.github.superz97.githubactivitytracker.entity.ActivityRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityRecordRepository extends JpaRepository<ActivityRecord, String> {

    List<ActivityRecord> findByUsernameOrderByEventTimeDesc(String username);

    List<ActivityRecord> findByUsernameAndEventTypeOrderByEventTimeDesc(String username, String eventType);

    @Query("""
    SELECT ar FROM ActivityRecord ar WHERE ar.username = :username
    AND ar.eventTime BETWEEN :startDate AND :endDate
    ORDER BY ar.eventTime DESC
""")
    List<ActivityRecord> findByUsernameAndDateRange(String username, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT DISTINCT ar.eventType FROM ActivityRecord ar WHERE ar.username = :username")
    List<String> findDistinctEventTypeByUsername(String username);


}
