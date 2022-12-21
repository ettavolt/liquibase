package liquibase.changelog;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ChangeLogHistoryServiceFactory {

    private static ChangeLogHistoryServiceFactory instance;

    private final Deque<ChangeLogHistoryService> registry = new ConcurrentLinkedDeque<>();

    private final Map<Database, ChangeLogHistoryService> services = new ConcurrentHashMap<>();

    public static synchronized ChangeLogHistoryServiceFactory getInstance() {
        if (instance == null) {
            instance = new ChangeLogHistoryServiceFactory();
        }
        return instance;
    }

    /**
     * Set the instance used by this singleton. Used primarily for testing.
     */
    public static synchronized void setInstance(ChangeLogHistoryServiceFactory changeLogHistoryServiceFactory) {
        ChangeLogHistoryServiceFactory.instance = changeLogHistoryServiceFactory;
    }


    public static synchronized void reset() {
        instance = null;
    }

    private ChangeLogHistoryServiceFactory() {
        try {
            for (ChangeLogHistoryService service : Scope.getCurrentScope().getServiceLocator().findInstances(ChangeLogHistoryService.class)) {
                register(service);
            }

        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public void register(ChangeLogHistoryService changeLogHistoryService) {
        registry.addFirst(changeLogHistoryService);
    }

    private ChangeLogHistoryService selectFor(Database database) {
            ChangeLogHistoryService exampleService = registry
                    .stream()
                    .filter(s -> s.supports(database))
                    .max(Comparator.comparing(ChangeLogHistoryService::getPriority))
                    .orElseThrow(() -> new UnexpectedLiquibaseException(
                            "Cannot find ChangeLogHistoryService for " + database.getShortName()
                    ));

            try {
                Class<? extends ChangeLogHistoryService> aClass = exampleService.getClass();
                ChangeLogHistoryService service;
                try {
                    aClass.getConstructor();
                    service = aClass.getConstructor().newInstance();
                    service.setDatabase(database);
                } catch (NoSuchMethodException e) {
                    // must have been manually added to the registry and so already configured.
                    service = exampleService;
                }

                return service;
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }
    }

    public ChangeLogHistoryService getChangeLogService(Database database) {
        return services.computeIfAbsent(database, this::selectFor);
    }

    public void resetAll() {
        synchronized (ChangeLogHistoryServiceFactory.class) {
            for (ChangeLogHistoryService changeLogHistoryService : registry) {
                changeLogHistoryService.reset();
            }
            instance = null;
        }
    }
}

