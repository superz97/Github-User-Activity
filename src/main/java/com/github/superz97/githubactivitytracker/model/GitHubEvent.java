package com.github.superz97.githubactivitytracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
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

    @JsonIgnore
    public String getFormattedActivity() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(createdAt).append("] ");
        sb.append(actor.getLogin()).append(" ");
        try {
            switch (type) {
                case "PushEvent":
                    Object commitsObj = payload.get("commits");
                    int commitCount = 0;
                    if (commitsObj instanceof List) {
                        commitCount = ((List<?>) commitsObj).size();
                    } else if (payload.get("size") != null) {
                        commitCount = ((Number) payload.get("size")).intValue();
                    }
                    sb.append("pushed ").append(commitCount).append(" commit(s) to ").append(repo.getName());
                    break;
                case "CreateEvent":
                    String refType = (String) payload.get("ref_type");
                    String ref = (String) payload.get("ref");
                    sb.append("created ").append(refType != null ? refType : "unknown");
                    if (ref != null && !ref.isEmpty()) {
                        sb.append(" '").append(ref).append("'");
                    }
                    sb.append(" in ").append(repo.getName());
                    break;
                case "DeleteEvent":
                    String delRefType = (String) payload.get("ref_type");
                    String delRef = (String) payload.get("ref");
                    sb.append("deleted ").append(delRefType != null ? delRefType : "unknown");
                    if (delRef != null && !delRef.isEmpty()) {
                        sb.append(" '").append(delRef).append("'");
                    }
                    sb.append(" in ").append(repo.getName());
                    break;
                case "IssuesEvent":
                    String action = (String) payload.get("action");
                    Map<?, ?> issue = (Map<?, ?>) payload.get("issue");
                    sb.append(action != null ? action : "modified").append(" an issue");
                    if (issue != null && issue.get("title") != null) {
                        sb.append(" '").append(issue.get("title")).append("'");
                    }
                    sb.append(" in ").append(repo.getName());
                    break;
                case "PullRequestEvent":
                    String prAction = (String) payload.get("action");
                    Map<?, ?> pr = (Map<?, ?>) payload.get("pull_request");
                    sb.append(prAction != null ? prAction : "modified").append(" a pull request");
                    if (pr != null && pr.get("title") != null) {
                        sb.append(" '").append(pr.get("title")).append("'");
                    }
                    sb.append(" in ").append(repo.getName());
                    break;
                case "WatchEvent":
                    sb.append("starred ").append(repo.getName());
                    break;
                case "ForkEvent":
                    Map<?, ?> forkee = (Map<?, ?>) payload.get("forkee");
                    sb.append("forked ").append(repo.getName());
                    if (forkee != null && forkee.get("full_name") != null) {
                        sb.append(" to ").append(forkee.get("full_name"));
                    }
                    break;
                case "IssueCommentEvent":
                    String commentAction = (String) payload.get("action");
                    Map<?, ?> commentIssue = (Map<?, ?>) payload.get("issue");
                    sb.append(commentAction != null && !commentAction.equals("created") ? commentAction : "commented on");
                    sb.append(" an issue");
                    if (commentIssue != null && commentIssue.get("title") != null) {
                        sb.append(" '").append(commentIssue.get("title")).append("'");
                    }
                    sb.append(" in ").append(repo.getName());
                    break;
                case "PullRequestReviewEvent":
                    String reviewAction = (String) payload.get("action");
                    sb.append(reviewAction != null ? reviewAction : "reviewed").append(" a pull request in ").append(repo.getName());
                    break;
                case "PullRequestReviewCommentEvent":
                    sb.append("commented on a pull request review in ").append(repo.getName());
                    break;
                case "CommitCommentEvent":
                    sb.append("commented on a commit in ").append(repo.getName());
                    break;
                case "ReleaseEvent":
                    String releaseAction = (String) payload.get("action");
                    Map<?, ?> release = (Map<?, ?>) payload.get("release");
                    sb.append(releaseAction != null ? releaseAction : "created").append(" a release");
                    if (release != null && release.get("tag_name") != null) {
                        sb.append(" '").append(release.get("tag_name")).append("'");
                    }
                    sb.append(" in ").append(repo.getName());
                    break;
                case "PublicEvent":
                    sb.append("made ").append(repo.getName()).append(" public");
                    break;
                case "MemberEvent":
                    String memberAction = (String) payload.get("action");
                    Map<?, ?> member = (Map<?, ?>) payload.get("member");
                    sb.append(memberAction != null ? memberAction : "added").append(" ");
                    if (member != null && member.get("login") != null) {
                        sb.append(member.get("login")).append(" as a ");
                    }
                    sb.append("member to ").append(repo.getName());
                    break;
                case "GollumEvent":
                    List<?> pages = (List<?>) payload.get("pages");
                    if (pages != null && !pages.isEmpty()) {
                        sb.append("updated ").append(pages.size()).append(" wiki page(s) in ").append(repo.getName());
                    } else {
                        sb.append("updated wiki in ").append(repo.getName());
                    }
                    break;
                default:
                    sb.append("performed ").append(type).append(" on ").append(repo.getName());
            }
        } catch (Exception e) {
            sb.append("performed ").append(type).append(" on ").append(repo.getName());
        }
        return sb.toString();
    }

}
