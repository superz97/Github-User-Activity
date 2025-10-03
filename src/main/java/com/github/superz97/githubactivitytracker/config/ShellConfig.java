package com.github.superz97.githubactivitytracker.config;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Configuration
public class ShellConfig {

    @Component
    public static class CustomPromptProvider implements PromptProvider {
        @Override
        public AttributedString getPrompt() {
            return new AttributedString(
                    "github-tracker> ",
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN).bold()
            );
        }
    }

}
