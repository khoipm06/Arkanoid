package com.arkanoid.database;

import com.arkanoid.database.repository.*;
import com.arkanoid.database.repository.impl.*;
import com.arkanoid.database.service.AuthenticationService;
import com.arkanoid.database.service.PasswordService;

/**
 * Factory for creating database repository and service instances.
 * Implements a simple Dependency Injection container.
 */
public class RepositoryFactory {
    private static RepositoryFactory instance;

    private final DatabaseManager dbManager;
    private final UserRepository userRepository;
    private final PlayerProfileRepository profileRepository;
    private final GameSaveRepository gameSaveRepository;
    private final PasswordService passwordService;
    private final AuthenticationService authService;

    private RepositoryFactory() {
        this.dbManager = DatabaseManager.getInstance();
        this.dbManager.initialize();

        // Initialize repositories
        this.userRepository = new UserRepositoryImpl(dbManager);
        this.profileRepository = new PlayerProfileRepositoryImpl(dbManager);
        this.gameSaveRepository = new GameSaveRepositoryImpl(dbManager);

        // Initialize services
        this.passwordService = new PasswordService();
        this.authService = new AuthenticationService(userRepository, profileRepository, passwordService);
    }

    public static synchronized RepositoryFactory getInstance() {
        if (instance == null) {
            instance = new RepositoryFactory();
        }
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return dbManager;
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

    public PasswordService getPasswordService() {
        return passwordService;
    }

    public AuthenticationService getAuthenticationService() {
        return authService;
    }

    /**
     * Close all database connections
     */
    public void shutdown() {
        dbManager.close();
    }
}
