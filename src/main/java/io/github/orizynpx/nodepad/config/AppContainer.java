package io.github.orizynpx.nodepad.config;

import io.github.orizynpx.nodepad.domain.logic.GraphLayoutEngine;
import io.github.orizynpx.nodepad.domain.logic.TaskMutatorService; // NEW IMPORT
import io.github.orizynpx.nodepad.domain.logic.TextParser;
import io.github.orizynpx.nodepad.domain.port.FileSystemRepository;
import io.github.orizynpx.nodepad.domain.port.FileRepository; // NEW IMPORT
import io.github.orizynpx.nodepad.domain.port.MetadataRepository;
import io.github.orizynpx.nodepad.domain.port.TagStrategy;
import io.github.orizynpx.nodepad.infrastructure.external.OpenLibraryClient;
import io.github.orizynpx.nodepad.infrastructure.persistence.DatabaseConnection;
import io.github.orizynpx.nodepad.infrastructure.persistence.LocalFileSystemRepository;
import io.github.orizynpx.nodepad.infrastructure.persistence.SqliteFileDao; // NEW IMPORT
import io.github.orizynpx.nodepad.infrastructure.persistence.SqliteMetadataRepository;
import io.github.orizynpx.nodepad.infrastructure.text.IsbnTagStrategy;
import io.github.orizynpx.nodepad.infrastructure.text.UrlTagStrategy;

import java.util.List;

public class AppContainer {

    private static AppContainer instance;

    // --- Repositories (Ports) ---
    private final MetadataRepository metadataRepository;
    private final FileSystemRepository fileSystemRepository;
    private final FileRepository fileRepository; // NEW FIELD

    // --- Services (Logic/External) ---
    private final TextParser textParser;
    private final GraphLayoutEngine graphLayoutEngine;
    private final OpenLibraryClient openLibraryClient;
    private final TaskMutatorService taskMutatorService; // NEW FIELD

    private AppContainer() {
        // 1. Init Infrastructure
        DatabaseConnection.getInstance();

        // 2. Init Repositories
        this.metadataRepository = new SqliteMetadataRepository();
        this.fileSystemRepository = new LocalFileSystemRepository();
        this.fileRepository = new SqliteFileDao();

        // 3. Init Logic
        List<TagStrategy> tagStrategies = List.of(new IsbnTagStrategy(), new UrlTagStrategy());
        this.textParser = new TextParser(tagStrategies);
        this.graphLayoutEngine = new GraphLayoutEngine();

        // --- MISSING LINK: Initialize Service ---
        this.taskMutatorService = new TaskMutatorService();

        // 4. External
        this.openLibraryClient = new OpenLibraryClient();
    }

    public static synchronized AppContainer getInstance() {
        if (instance == null) {
            instance = new AppContainer();
        }
        return instance;
    }

    // --- Accessors for UI Controllers ---

    public MetadataRepository getMetadataRepository() {
        return metadataRepository;
    }

    public FileSystemRepository getFileSystemRepository() {
        return fileSystemRepository;
    }

    public FileRepository getFileRepository() {
        return fileRepository;
    } // NEW GETTER

    public TextParser getTextParser() {
        return textParser;
    }

    public GraphLayoutEngine getGraphLayoutEngine() {
        return graphLayoutEngine;
    }

    public OpenLibraryClient getOpenLibraryClient() {
        return openLibraryClient;
    }

    public TaskMutatorService getTaskMutatorService() {
        return taskMutatorService;
    }
}