package com.arkanoid.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for managing database backups and restores during testing.
 * Uses external shell scripts (dh-helper.ps1 for Windows, dh-helper.sh for Unix)
 * to perform backup and restore operations.
 */
public class TestDatabaseHelper {

    private static final String SCRIPT_BASE_NAME = "src/test/resources/scripts/db-helper";
    
    /**
     * Creates a backup of the test database.
     * Should be called in @BeforeAll methods.
     */
    public static void backupDatabase() {
        runScript("backup");
    }
    
    /**
     * Restores the database from backup and cleans up the backup file.
     * Should be called in @AfterAll methods.
     */
    public static void restoreDatabase() {
        runScript("restore");
    }
    
    /**
     * Runs the database management script with the specified action.
     * 
     * @param action The action to perform ("backup" or "restore")
     */
    private static void runScript(String action) {
        String cwd = System.getProperty("user.dir");
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String scriptName = SCRIPT_BASE_NAME + (isWindows ? ".ps1" : ".sh");
        File scriptFile = new File(cwd, scriptName);

        if (!scriptFile.exists() || !scriptFile.isFile()) {
            System.err.println("[WARNING] Script not found: " + scriptFile.getAbsolutePath());
            return;
        }

        List<String> command = buildCommand(isWindows, scriptFile.getAbsolutePath(), action);
        executeCommand(command, cwd);
    }
    
    /**
     * Builds the command to execute based on the platform.
     */
    private static List<String> buildCommand(boolean isWindows, String scriptPath, String action) {
        if (isWindows) {
            return Arrays.asList(
                "powershell.exe", 
                "-NoProfile", 
                "-ExecutionPolicy", 
                "Bypass", 
                "-File", 
                scriptPath,
                action
            );
        } else {
            return Arrays.asList("bash", scriptPath, action);
        }
    }
    
    /**
     * Executes the command and prints output to console.
     */
    private static void executeCommand(List<String> command, String workingDir) {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(workingDir));
        pb.redirectErrorStream(true);

        try {
            Process proc = pb.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
            int exitCode = proc.waitFor();
            if (exitCode != 0) {
                System.err.println("[ERROR] Script exited with code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("[ERROR] Failed to run script: " + e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
