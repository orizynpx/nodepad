package io.github.orizynpx.nodepad.app;

import io.github.orizynpx.nodepad.dao.*;
import io.github.orizynpx.nodepad.dao.impl.SqliteBookRepository;
import io.github.orizynpx.nodepad.dao.impl.SqliteFileRepository;
import io.github.orizynpx.nodepad.dao.impl.SqliteLinkRepository;
import io.github.orizynpx.nodepad.service.LinkPreviewService;
import io.github.orizynpx.nodepad.service.OpenLibraryService;
import io.github.orizynpx.nodepad.service.ParserService;
import io.github.orizynpx.nodepad.dao.impl.FileContentRepository;
import io.github.orizynpx.nodepad.service.TaskService;

public class ServiceRegistry {
    private static final ServiceRegistry INSTANCE = new ServiceRegistry();

    // INTERFACES (Abstractions)
    private final FileRepository fileRepository;
    private final BookRepository bookRepository;
    private final ContentRepository contentRepository;
    private final LinkRepository linkRepository;

    // SERVICES
    private final ParserService parserService;
    private final OpenLibraryService openLibraryService;
    private final TaskService taskService;
    private final LinkPreviewService linkPreviewService;

    private ServiceRegistry() {
        // CONCRETE IMPLEMENTATIONS (The only place using 'new')
        DatabaseFactory databaseFactory = new DatabaseManager();

        this.fileRepository = new SqliteFileRepository(databaseFactory);
        this.bookRepository = new SqliteBookRepository(databaseFactory);
        this.contentRepository = new FileContentRepository();
        this.parserService = new ParserService();
        this.openLibraryService = new OpenLibraryService(this.bookRepository);
        this.taskService = new TaskService();
        this.linkRepository = new SqliteLinkRepository(databaseFactory);
        this.linkPreviewService = new LinkPreviewService(this.linkRepository);
    }

    public static ServiceRegistry getInstance() {
        return INSTANCE;
    }

    public FileRepository getFileRepository() {
        return fileRepository;
    }

    public BookRepository getBookRepository() {
        return bookRepository;
    }

    public LinkRepository getLinkRepository() {
        return linkRepository;
    }

    public ParserService getParserService() {
        return parserService;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public TaskService getTaskMutatorService() {
        return taskService;
    }

    public OpenLibraryService getOpenLibraryService() {
        return openLibraryService;
    }

    public LinkPreviewService getLinkPreviewService() {
        return linkPreviewService;
    }
}