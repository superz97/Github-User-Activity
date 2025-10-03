package com.github.superz97.githubactivitytracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Actor {

    private String login;

    @JsonProperty("display_login")
    private String displayLogin;

    @JsonProperty("avatar_url")
    private String avatarUrl;

}
