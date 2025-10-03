package com.github.superz97.githubactivitytracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repo {

    private String name;
    private String url;

}
