package io.github.orizynpx.nodepad.app;

import java.util.HashSet;
import java.util.Set;

public class ProjectContext {
    private static final ProjectContext INSTANCE = new ProjectContext();
    private final Set<String> activeIsbns = new HashSet<>();
    private final Set<String> activeUrls = new HashSet<>();

    private ProjectContext() {
    }

    public static ProjectContext getInstance() {
        return INSTANCE;
    }

    public void updateContext(Set<String> isbns, Set<String> urls) {
        this.activeIsbns.clear();
        this.activeIsbns.addAll(isbns);

        this.activeUrls.clear();
        this.activeUrls.addAll(urls);
    }

    public Set<String> getActiveIsbns() {
        return new HashSet<>(activeIsbns);
    }

    public Set<String> getActiveUrls() {
        return new HashSet<>(activeUrls);
    }
}