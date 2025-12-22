package io.github.orizynpx.nodepad.app;

import io.github.orizynpx.nodepad.dao.*;
import io.github.orizynpx.nodepad.service.OpenLibraryService;
import io.github.orizynpx.nodepad.service.ParserService;
import io.github.orizynpx.nodepad.dao.FileContentRepository;
import io.github.orizynpx.nodepad.service.TaskMutatorService;

public class ServiceRegistry {
    private static final ServiceRegistry INSTANCE = new ServiceRegistry();

    // INTERFACES (Abstractions)
    private final FileRepository fileRepository;
    private final BookRepository bookRepository;
    private final ContentRepository contentRepository;

    // SERVICES
    private final ParserService parserService;
    private final OpenLibraryService openLibraryService;
    private final TaskMutatorService taskMutatorService;

    private ServiceRegistry() {
        // CONCRETE IMPLEMENTATIONS (The only place using 'new')
        ConnectionFactory connectionFactory = new DatabaseManager();

        this.fileRepository = new SqliteFileDao(connectionFactory);
        this.bookRepository = new SqliteBookDao(connectionFactory);
        this.contentRepository = new FileContentRepository();
        this.parserService = new ParserService();
        this.openLibraryService = new OpenLibraryService(this.bookRepository);
        this.taskMutatorService = new TaskMutatorService();
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

    public ParserService getParserService() {
        return parserService;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public TaskMutatorService getTaskMutatorService() {
        return taskMutatorService;
    }

    public OpenLibraryService getOpenLibraryService() {
        return openLibraryService;
    }
}