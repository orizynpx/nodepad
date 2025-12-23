package io.github.orizynpx.nodepad.config;

import io.github.orizynpx.nodepad.domain.logic.GraphLayoutEngine;
import io.github.orizynpx.nodepad.domain.logic.TaskMutatorService;
import io.github.orizynpx.nodepad.domain.logic.TextParser;
import io.github.orizynpx.nodepad.domain.port.*;
import io.github.orizynpx.nodepad.infrastructure.external.OpenLibraryClient;
import io.github.orizynpx.nodepad.infrastructure.persistence.*;
import io.github.orizynpx.nodepad.infrastructure.text.IsbnTagStrategy;
import io.github.orizynpx.nodepad.infrastructure.text.UrlTagStrategy;
import io.github.orizynpx.nodepad.service.LinkPreviewService; // Added

import java.util.List;

public class AppContainer {

    private static AppContainer instance;

    // Repositories
    private final MetadataRepository metadataRepository; // Books
    private final LinkRepository linkRepository;         // Links (Added)
    private final FileSystemRepository fileSystemRepository;
    private final FileRepository fileRepository;

    // Services
    private final TextParser textParser;
    private final GraphLayoutEngine graphLayoutEngine;
    private final OpenLibraryClient openLibraryClient;
    private final TaskMutatorService taskMutatorService;
    private final LinkPreviewService linkPreviewService; // Added

    private AppContainer() {
        DatabaseConnection conn = DatabaseConnection.getInstance();

        // Init Repositories
        this.metadataRepository = new SqliteMetadataRepository();
        this.linkRepository = new SqliteLinkDao(conn::getConnection); // Reuse connection logic
        this.fileSystemRepository = new LocalFileSystemRepository();
        this.fileRepository = new SqliteFileDao();

        // Init Logic
        List<TagStrategy> tagStrategies = List.of(new IsbnTagStrategy(), new UrlTagStrategy());
        this.textParser = new TextParser(tagStrategies);
        this.graphLayoutEngine = new GraphLayoutEngine();
        this.taskMutatorService = new TaskMutatorService();

        // Init External Services
        this.openLibraryClient = new OpenLibraryClient();
        this.linkPreviewService = new LinkPreviewService(this.linkRepository);
    }

    public static synchronized AppContainer getInstance() {
        if (instance == null) instance = new AppContainer();
        return instance;
    }

    // Getters
    public MetadataRepository getMetadataRepository() {
        return metadataRepository;
    }

    public LinkRepository getLinkRepository() {
        return linkRepository;
    }

    public FileSystemRepository getFileSystemRepository() {
        return fileSystemRepository;
    }

    public FileRepository getFileRepository() {
        return fileRepository;
    }

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

    public LinkPreviewService getLinkPreviewService() {
        return linkPreviewService;
    }
}