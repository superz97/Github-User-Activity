package com.github.superz97.githubactivitytracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubEvent {

    private String id;
    private String type;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    private Actor actor;
    private Repo repo;
    private Map<String, Object> payload;

    public String getFormattedActivity() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(createdAt).append("] ");
        sb.append(actor.getLogin()).append(" ");
        switch (type) {
            case "PushEvent":
                Integer size = (Integer) ((Map<?, ?>) payload.get("commits")).get("size");
                sb.append("pushed ").append(size != null ? size : 0).append(" commit(s) to ").append(repo.getName());
                break;
            case "CreateEvent":
                String refType = (String) payload.get("ref_type");
                sb.append("created ").append(refType != null ? refType : "unknown").append(" in ").append(repo.getName());
                break;
            case "DeleteEvent":
                String delRefType = (String) payload.get("ref_type");
                sb.append("deleted ").append(delRefType != null ? delRefType : "unknown").append(" in ").append(repo.getName());
                break;
            case "IssuesEvent":
                String action = (String) payload.get("action");
                sb.append(action != null ? action : "modified").append(" an issue in ").append(repo.getName());
                break;
            case "PullRequestEvent":
                String prAction = (String) payload.get("action");
                sb.append(prAction != null ? prAction : "modified").append(" a pull request in ").append(repo.getName());
                break;
            case "WatchEvent":
                sb.append("starred ").append(repo.getName());
                break;
            case "ForkEvent":
                sb.append("forked ").append(repo.getName());
                break;
            case "IssueCommentEvent":
                sb.append("commented on an issue in ").append(repo.getName());
                break;
            case "PublicEvent":
                sb.append("made ").append(repo.getName()).append(" public");
                break;
            default:
                sb.append("performed ").append(type).append(" on ").append(repo.getName());
        }
        return sb.toString();
    }

}
