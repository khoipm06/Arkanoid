package com.arkanoid.ui.view;

import com.arkanoid.systems.GameManager;
import com.arkanoid.ui.GameScene;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class Pause {
    @FXML
    private Button newGameButton;
    @FXML
    private Button resumeButton;
    @FXML
    private Button quitButton;

    private GameScene gameScene;
    private Stage stage;
    private GameManager gameManager;

    public void init(GameScene gameScene, Stage stage, GameManager gameManager) {
        this.gameScene = gameScene;
        this.stage = stage;
        this.gameManager = gameManager;
    }

    @FXML
    public void onNewGameClick(MouseEvent event) {
        int currentLevel = gameManager.getLevelNumber();
        GameScene newScene = new GameScene(stage, stage.getWidth(), stage.getHeight(), currentLevel);
        newScene.start();
        stage.setScene(newScene.getScene());
    }
    @FXML
    public void onResumeClick(MouseEvent event) {
        gameManager.resume();
        gameScene.hidePauseOverlay();
    }
    @FXML
    public void onQuitClick(MouseEvent event) {
        SceneManager.switchTo("mainMenuView");
    }
}
