package com.arkanoid.ui.view;

import com.arkanoid.database.entity.GameSave;
import com.arkanoid.systems.GameManager;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.save.GameSaveManager;
import com.arkanoid.systems.save.impl.GameSaveManagerImpl;
import com.arkanoid.systems.sound.SoundManager;
import com.arkanoid.ui.components.ToastNotification;
import com.arkanoid.ui.GameScene;
import com.google.gson.JsonSyntaxException;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Controller for the Save/Load scene. Handles save game creation, loading, and
 * deletion.
 */
public class SaveLoadScene {
    private static final Logger logger = GameLogger.getLogger(SaveLoadScene.class);
    private static final SoundManager soundManager = SoundManager.getInstance();

    @FXML
    private Label saveCountLabel;

    @FXML
    private Button saveNewButton;

    @FXML
    private ListView<GameSave> saveListView;

    @FXML
    private Button loadButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button backButton;

    private GameSaveManager gameSaveManager;
    private GameManager gameManager;
    private Runnable onBackCallback;
    private int currentUserId;
    private StackPane rootPane;
    private GameScene gameScene;

    /**
     * Initialize the controller with required dependencies.
     */
    public void init(GameSaveManager gameSaveManager, GameManager gameManager, Stage stage, int userId,
            Runnable onBackCallback, StackPane rootPane, GameScene gameScene) {
        this.gameSaveManager = gameSaveManager;
        this.gameManager = gameManager;
        this.currentUserId = userId;
        this.onBackCallback = onBackCallback;
        this.rootPane = rootPane;
        this.gameScene = gameScene;

        // Set custom cell factory for visual thumbnails
        saveListView.setCellFactory(lv -> new GameSaveListCell());

        // Load saves into ListView
        refreshSaveList();

        // Enable/disable buttons based on selection
        saveListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            loadButton.setDisable(!hasSelection);
            deleteButton.setDisable(!hasSelection);
        });

        // Add keyboard shortcuts
        setupKeyboardShortcuts();
    }

    /**
     * Sets up keyboard shortcuts for the save/load scene. DELETE key - Delete
     * selected save
     */
    private void setupKeyboardShortcuts() {
        saveListView.setOnKeyPressed(event -> {
            switch (event.getCode()) {
            case DELETE:
                if (!deleteButton.isDisabled()) {
                    onDeleteGame(new ActionEvent());
                }
                event.consume();
                break;
            case ENTER:
                if (!loadButton.isDisabled()) {
                    onLoadGame(new ActionEvent());
                }
                event.consume();
                break;
            default:
                break;
            }
        });
    }

    /**
     * Refreshes the save list and updates the save count label.
     */
    private void refreshSaveList() {
        // Use async method if available
        if (gameSaveManager instanceof GameSaveManagerImpl) {
            GameSaveManagerImpl impl = (GameSaveManagerImpl) gameSaveManager;
            impl.getAllSavesAsync(currentUserId).thenAccept(saves -> {
                Platform.runLater(() -> {
                    saveListView.setItems(saves);
                    int saveCount = gameSaveManager.getSaveCount(currentUserId);
                    saveCountLabel.setText(saveCount + "/100 saves");
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> showError("Failed to load saves: " + ex.getMessage()));
                return null;
            });
        } else {
            // Fallback to synchronous method
            ObservableList<GameSave> saves = gameSaveManager.getAllSaves(currentUserId);
            saveListView.setItems(saves);
            int saveCount = gameSaveManager.getSaveCount(currentUserId);
            saveCountLabel.setText(saveCount + "/100 saves");
        }
    }

    @FXML
    public void onSaveNewGame(ActionEvent event) {
        soundManager.playSound("Accept.wav");

        // Check if game can be saved
        if (!gameSaveManager.canSaveCurrentGame()) {
            showError("Cannot save game in current state");
            return;
        }

        // Prompt for save name
        TextInputDialog dialog = new TextInputDialog("Level " + gameManager.getLevelNumber());
        dialog.setTitle("Save Game");
        dialog.setHeaderText("Enter a name for this save:");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String saveName = result.get().trim();

            if (!gameSaveManager.isValidSaveName(saveName)) {
                showError("Invalid save name. Must be 1-50 characters.");
                return;
            }

            // Capture thumbnail from game canvas
            WritableImage canvasSnapshot = null;
            if (gameScene != null) {
                try {
                    canvasSnapshot = gameScene.captureCanvasSnapshot();
                } catch (Exception thumbEx) {
                    logger.error("Failed to capture thumbnail: {}", thumbEx.getMessage());
                    // Continue without thumbnail
                }
            }

            // Use async save if available
            if (gameSaveManager instanceof GameSaveManagerImpl) {
                GameSaveManagerImpl impl = (GameSaveManagerImpl) gameSaveManager;
                
                // Show loading indicator
                saveNewButton.setDisable(true);
                ToastNotification.showToast("Saving game...", rootPane, ToastNotification.ToastType.INFO);
                
                impl.saveCurrentGameAsync(currentUserId, saveName, canvasSnapshot)
                    .thenAccept(savedGame -> {
                        Platform.runLater(() -> {
                            saveNewButton.setDisable(false);
                            refreshSaveList();
                            showSuccess("Game saved successfully!");
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            saveNewButton.setDisable(false);
                            showError("Failed to save game: " + ex.getMessage());
                        });
                        return null;
                    });
            } else {
                // Fallback to synchronous save
                try {
                    gameSaveManager.saveCurrentGame(currentUserId, saveName, canvasSnapshot);
                    refreshSaveList();
                    showSuccess("Game saved successfully!");
                } catch (Exception e) {
                    showError("Failed to save game: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    public void onLoadGame(ActionEvent event) {
        soundManager.playSound("Accept.wav");

        GameSave selectedSave = saveListView.getSelectionModel().getSelectedItem();
        if (selectedSave == null) {
            showError("Please select a save to load");
            return;
        }

        // Use async load if available
        if (gameSaveManager instanceof GameSaveManagerImpl) {
            GameSaveManagerImpl impl = (GameSaveManagerImpl) gameSaveManager;
            
            // Show loading indicator
            loadButton.setDisable(true);
            deleteButton.setDisable(true);
            saveNewButton.setDisable(true);
            ToastNotification.showToast("Loading game...", rootPane, ToastNotification.ToastType.INFO);
            
            impl.loadGameAsync(selectedSave.getId())
                .thenAccept(loadedSave -> {
                    Platform.runLater(() -> {
                        loadButton.setDisable(false);
                        deleteButton.setDisable(false);
                        saveNewButton.setDisable(false);
                        showSuccess("Game loaded successfully!");
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        loadButton.setDisable(false);
                        deleteButton.setDisable(false);
                        saveNewButton.setDisable(false);
                        
                        // Handle different error types
                        Throwable cause = ex.getCause();
                        if (cause instanceof JsonSyntaxException) {
                            showError("Save file is corrupted");
                        } else if (cause instanceof IllegalArgumentException) {
                            if (cause.getMessage().contains("not found")) {
                                showError("Save not found");
                            } else {
                                showError("Invalid save: " + cause.getMessage());
                            }
                        } else if (cause instanceof IllegalStateException) {
                            showError("Save file is corrupted - invalid game state");
                        } else {
                            showError("Failed to load game: " + ex.getMessage());
                        }
                    });
                    return null;
                });
        } else {
            // Fallback to synchronous load
            try {
                gameSaveManager.loadGame(selectedSave.getId());
                showSuccess("Game loaded successfully!");
            } catch (JsonSyntaxException e) {
                showError("Save file is corrupted");
            } catch (IllegalArgumentException e) {
                if (e.getMessage().contains("not found")) {
                    showError("Save not found");
                } else {
                    showError("Invalid save: " + e.getMessage());
                }
            } catch (IllegalStateException e) {
                showError("Save file is corrupted - invalid game state");
            } catch (Exception e) {
                showError("Failed to load game: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void onDeleteGame(ActionEvent event) {
        soundManager.playSound("Accept.wav");

        GameSave selectedSave = saveListView.getSelectionModel().getSelectedItem();
        if (selectedSave == null) {
            showError("Please select a save to delete");
            return;
        }

        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete save: " + selectedSave.getSaveName() + "?");
        confirmDialog.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int saveId = selectedSave.getId();
            
            // Use async delete if available
            if (gameSaveManager instanceof GameSaveManagerImpl) {
                GameSaveManagerImpl impl = (GameSaveManagerImpl) gameSaveManager;
                
                // Disable buttons during deletion
                deleteButton.setDisable(true);
                loadButton.setDisable(true);
                
                impl.deleteSaveAsync(saveId)
                    .thenAccept(v -> {
                        Platform.runLater(() -> {
                            // Clear from thumbnail cache
                            GameSaveListCell.removeCachedThumbnail((long) saveId);
                            refreshSaveList();
                            showSuccess("Save deleted successfully!");
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            deleteButton.setDisable(false);
                            loadButton.setDisable(false);
                            showError("Failed to delete save: " + ex.getMessage());
                        });
                        return null;
                    });
            } else {
                // Fallback to synchronous delete
                try {
                    gameSaveManager.deleteSave(saveId);
                    GameSaveListCell.removeCachedThumbnail((long) saveId);
                    refreshSaveList();
                    showSuccess("Save deleted successfully!");
                } catch (Exception e) {
                    showError("Failed to delete save: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    public void onBack(ActionEvent event) {
        soundManager.playSound("Accept.wav");
        if (onBackCallback != null) {
            onBackCallback.run();
        }
    }

    private void showError(String message) {
        logger.error("Save/Load error: {}", message);
        if (rootPane != null) {
            ToastNotification.showToast(message, rootPane, ToastNotification.ToastType.ERROR);
        }
    }

    private void showSuccess(String message) {
        logger.info("Save/Load success: {}", message);
        if (rootPane != null) {
            ToastNotification.showToast(message, rootPane, ToastNotification.ToastType.SUCCESS);
        }
    }
}
