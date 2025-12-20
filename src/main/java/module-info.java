module com.arkanoid {
    // Export all packages for full modular compatibility
    exports com.arkanoid;
    exports com.arkanoid.core.components;
    exports com.arkanoid.core.entities;
    exports com.arkanoid.core.physics;
    exports com.arkanoid.database;
    exports com.arkanoid.database.entity;
    exports com.arkanoid.database.exception;
    exports com.arkanoid.database.repository;
    exports com.arkanoid.database.repository.impl;
    exports com.arkanoid.debug;
    exports com.arkanoid.systems;
    exports com.arkanoid.systems.input;
    exports com.arkanoid.systems.level;
    exports com.arkanoid.systems.logging;
    exports com.arkanoid.systems.player;
    exports com.arkanoid.systems.save;
    exports com.arkanoid.systems.save.impl;
    exports com.arkanoid.systems.sound;
    exports com.arkanoid.systems.threading;
    exports com.arkanoid.systems.twoplayer;
    exports com.arkanoid.ui;
    exports com.arkanoid.ui.components;
    exports com.arkanoid.ui.view;
    exports com.arkanoid.utils;

    // Open packages for reflection (JavaFX FXML, Gson, etc.)
    opens com.arkanoid to javafx.fxml, com.google.gson;
    opens com.arkanoid.ui.view to javafx.fxml, com.google.gson;
    opens com.arkanoid.database.entity to com.google.gson;
    opens com.arkanoid.systems.save to com.google.gson;
    opens com.arkanoid.systems.player to com.google.gson;

    // Java SE modules
    requires transitive java.sql;
    requires java.desktop;
    requires java.logging;
    requires java.management;
    requires java.naming;

    // JavaFX modules
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive javafx.media;
    requires transitive javafx.swing;

    // Third-party library modules
    requires transitive com.google.gson;
    requires transitive org.xerial.sqlitejdbc;
    requires transitive org.lz4.java;
    
    // Logging framework modules
    requires transitive org.slf4j;
    requires transitive ch.qos.logback.classic;
    requires transitive ch.qos.logback.core;

    // Service providers
    uses java.sql.Driver;
}
