package com.arkanoid.database;

import com.arkanoid.database.repository.*;
import com.arkanoid.database.repository.impl.*;
import com.arkanoid.utils.PasswordHasher;

/**
 * Factory for creating database repository and service instances. Implements a
 * simple Dependency Injection container.
 */
public class RepositoryFactory {
    private static RepositoryFactory instance;

    private final DatabaseManager databaseManager;
    private final UserRepository userRepository;
    private final PlayerProfileRepository profileRepository;
    private final GameSaveRepository gameSaveRepository;
    private final PasswordHasher passwordHasher;

    private RepositoryFactory() {
        this.databaseManager = DatabaseManager.getInstance();
        this.databaseManager.initialize();

        // Initialize repositories
        this.userRepository = new UserRepositoryImpl(databaseManager);
        this.profileRepository = new PlayerProfileRepositoryImpl(databaseManager);
        this.gameSaveRepository = new GameSaveRepositoryImpl(databaseManager);

        // Initialize services
        this.passwordHasher = new PasswordHasher();
    }

    public static synchronized RepositoryFactory getInstance() {
        if (instance == null) {
            instance = new RepositoryFactory();
        }
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public PlayerProfileRepository getPlayerProfileRepository() {
        return profileRepository;
    }

    public GameSaveRepository getGameSaveRepository() {
        return gameSaveRepository;
    }

    public PasswordHasher getPasswordHasher() {
        return passwordHasher;
    }

    /**
     * Close all database connections
     */
    public void shutdown() {
        databaseManager.close();
    }
}
