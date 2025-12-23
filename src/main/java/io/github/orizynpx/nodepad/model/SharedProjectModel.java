package io.github.orizynpx.nodepad.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import java.util.Set;

public class SharedProjectModel {
    private final ObservableSet<String> activeIsbns = FXCollections.observableSet();
    private final ObservableSet<String> activeUrls = FXCollections.observableSet();

    public void updateContext(Set<String> isbns, Set<String> urls) {
        this.activeIsbns.clear();
        this.activeIsbns.addAll(isbns);

        this.activeUrls.clear();
        this.activeUrls.addAll(urls);
    }

    public ObservableSet<String> getActiveIsbns() {
        return activeIsbns;
    }

    public ObservableSet<String> getActiveUrls() {
        return activeUrls;
    }
}