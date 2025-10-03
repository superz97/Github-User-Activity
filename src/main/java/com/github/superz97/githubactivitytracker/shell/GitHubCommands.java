package com.github.superz97.githubactivitytracker.shell;

import com.github.superz97.githubactivitytracker.model.GitHubEvent;
import com.github.superz97.githubactivitytracker.service.ActivityService;
import com.github.superz97.githubactivitytracker.service.CacheService;
import com.github.superz97.githubactivitytracker.service.GitHubService;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@ShellComponent
@RequiredArgsConstructor
public class GitHubCommands {

    private final ActivityService activityService;
    private final GitHubService gitHubService;
    private final CacheService cacheService;

    @ShellMethod(value = "Fetch and display GitHub user activity", key = {"activity", "a"})
    public String fetchActivity(
            @ShellOption(help = "GitHub username") String username,
            @ShellOption(help = "Maximum number of events to display", defaultValue = "10") int limit,
            @ShellOption(help = "Force refresh from GitHub API", defaultValue = "false") boolean refresh
    ) {
        StringBuilder result = new StringBuilder();
        result.append("\n📊 Fetching activity for user: ").append(username).append("\n");
        result.append("═".repeat(60)).append("\n");
        try {
            AtomicInteger counter = new AtomicInteger();
            activityService.getUserActivity(username, refresh)
                    .take(limit)
                    .doOnNext(event -> {
                        int count = counter.incrementAndGet();
                        result.append(String.format("%2d. ", count));
                        result.append(event.getFormattedActivity()).append("\n");
                    })
                    .blockLast(Duration.ofSeconds(30));
            if (counter.get() == 0) {
                result.append("❌ No activity found for user: ").append(username).append("\n");
            } else {
                result.append("═".repeat(60)).append("\n");
                result.append("✅ Total events displayed: ").append(counter.get()).append("\n");
                if (!refresh) {
                    result.append("💡 Tip: Use --refresh flag to force fetch from GitHub\n");
                }
            }
        } catch (Exception e) {
            result.append("❌ Error: ").append(e.getMessage()).append("\n");
            result.append("💡 Please check the username and try again\n");
        }
        return result.toString();
    }

    public String filterActivity(
            @ShellOption(help = "GitHub username") String username,
            @ShellOption(help = "Event type (e.g., PushEvent, CreateEvent, IssuesEvent)") String eventType,
            @ShellOption(help = "Maximum number of events to display", defaultValue = "10") int limit
    ) {
        StringBuilder result = new StringBuilder();
        result.append("\n🔍 Filtering ").append(eventType).append(" events for: ").append(username).append("\n");
        result.append("═".repeat(60)).append("\n");
        try {
            AtomicInteger counter = new AtomicInteger(0);
            activityService.getFilteredActivity(username, eventType)
                    .take(limit)
                    .doOnNext(event -> {
                        int count = counter.incrementAndGet();
                        result.append(String.format("%2d. ", count));
                        result.append(event.getFormattedActivity()).append("\n");
                    })
                    .blockLast(Duration.ofSeconds(30));
            if (counter.get() == 0) {
                result.append("❌ No ").append(eventType).append(" events found for user: ").append(username).append("\n");
            } else {
                result.append("═".repeat(60)).append("\n");
                result.append("✅ Total ").append(eventType).append(" events: ").append(counter.get()).append("\n");
            }
        } catch (Exception e) {
            result.append("❌ Error: ").append(e.getMessage()).append("\n");
        }
        return result.toString();
    }

    @ShellMethod(value = "Show event types for a user", key = {"types", "t"})
    public String showEventTypes(@ShellOption(help = "GitHub username") String username) {
        StringBuilder result = new StringBuilder();
        result.append("\n📋 Available event types for: ").append(username).append("\n");
        result.append("═".repeat(60)).append("\n");
        try {
            List<String> eventTypes = activityService.getAvailableEventTypes(username)
                    .block(Duration.ofSeconds(10));
            if (eventTypes == null || eventTypes.isEmpty()) {
                // Fetch fresh data if no historical data exists
                activityService.getUserActivity(username, true)
                        .collectList()
                        .block(Duration.ofSeconds(30));
                eventTypes = activityService.getAvailableEventTypes(username)
                        .block(Duration.ofSeconds(10));
            }
            if (eventTypes != null && !eventTypes.isEmpty()) {
                eventTypes.forEach(type -> result.append("  • ").append(type).append("\n"));
                result.append("═".repeat(60)).append("\n");
                result.append("💡 Use 'filter <username> <event-type>' to filter by type\n");
            } else {
                result.append("❌ No event types found. Try fetching activity first.\n");
            }
        } catch (Exception e) {
            result.append("❌ Error: ").append(e.getMessage()).append("\n");
        }
        return result.toString();
    }

    @ShellMethod(value = "Show activity summary for a user", key = {"summary", "s"})
    public String showSummary(@ShellOption(help = "GitHub username") String username) {
        StringBuilder result = new StringBuilder();
        result.append("\n📈 Activity Summary for: ").append(username).append("\n");
        result.append("═".repeat(60)).append("\n");
        try {
            List<GitHubEvent> events = activityService.getUserActivity(username, false)
                    .collectList()
                    .block(Duration.ofSeconds(30));
            if (events != null && !events.isEmpty()) {
                // Count by event type
                events.stream()
                        .collect(java.util.stream.Collectors.groupingBy(
                                GitHubEvent::getType,
                                java.util.stream.Collectors.counting()))
                        .forEach((type, count) ->
                                result.append(String.format("  %-20s: %d events\n", type, count)));
                result.append("═".repeat(60)).append("\n");
                result.append("📊 Total events: ").append(events.size()).append("\n");
                // Show repositories
                result.append("\n🏗️ Active repositories:\n");
                events.stream()
                        .map(e -> e.getRepo().getName())
                        .distinct()
                        .limit(5)
                        .forEach(repo -> result.append("  • ").append(repo).append("\n"));
                if (events.stream().map(e -> e.getRepo().getName()).distinct().count() > 5) {
                    result.append("  ... and more\n");
                }
            } else {
                result.append("❌ No activity found for user: ").append(username).append("\n");
            }
        } catch (Exception e) {
            result.append("❌ Error: ").append(e.getMessage()).append("\n");
        }
        return result.toString();
    }

    @ShellMethod(value = "Validate GitHub username", key = {"validate", "v"})
    public String validateUser(@ShellOption(help = "GitHub username") String username) {
        try {
            boolean isValid = Boolean.TRUE.equals(gitHubService.validateUser(username)
                    .block(Duration.ofSeconds(10)));
            if (isValid) {
                return "✅ User '" + username + "' exists on GitHub";
            } else {
                return "❌ User '" + username + "' not found on GitHub";
            }
        } catch (Exception e) {
            return "❌ Error validating user: " + e.getMessage();
        }
    }

    @ShellMethod(value = "Clear cache for a user", key = {"clear-cache", "cc"})
    public String clearCache(@ShellOption(help = "GitHub username") String username) {
        try {
            boolean cleared = Boolean.TRUE.equals(cacheService.invalidateCache(username)
                    .block(Duration.ofSeconds(5)));
            if (cleared) {
                return "✅ Cache cleared for user: " + username;
            } else {
                return "ℹ️ No cache found for user: " + username;
            }
        } catch (Exception e) {
            return "❌ Error clearing cache: " + e.getMessage();
        }
    }

    @ShellMethod(value = "Show command help", key = {"help", "h"})
    public String showHelp() {
        return """
            
            🚀 GitHub Activity Tracker - Available Commands
            ════════════════════════════════════════════════════════════
            
            📊 Activity Commands:
              activity <username> [--limit=10] [--refresh]
                Fetch and display user's GitHub activity
                
              filter <username> <event-type> [--limit=10]
                Filter activity by event type (e.g., PushEvent)
                
              summary <username>
                Show activity summary with statistics
                
              types <username>
                Display available event types for filtering
            
            🔧 Utility Commands:
              validate <username>
                Check if a GitHub username exists
                
              clear-cache <username>
                Clear cached data for a user
                
              help
                Show this help message
            
            💡 Event Types:
              • PushEvent - Code pushes
              • CreateEvent - Branch/tag creation
              • IssuesEvent - Issue interactions
              • PullRequestEvent - PR interactions
              • WatchEvent - Repository stars
              • ForkEvent - Repository forks
              • IssueCommentEvent - Issue comments
              • DeleteEvent - Branch/tag deletion
            
            ════════════════════════════════════════════════════════════
            """;
    }

}
