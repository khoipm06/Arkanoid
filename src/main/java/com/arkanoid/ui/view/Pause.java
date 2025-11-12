// package com.arkanoid.ui.view;

// import com.arkanoid.systems.GameManager;
// import com.arkanoid.systems.sound.SoundManager;
// import com.arkanoid.ui.GameScene;
// import javafx.fxml.FXML;
// import javafx.scene.control.Button;
// import javafx.scene.input.MouseEvent;
// import javafx.stage.Stage;

// public class Pause {
// private static final SoundManager soundManager = SoundManager.getInstance();

// @FXML
// private Button newGameButton;
// @FXML
// private Button gameSavesButton;
// @FXML
// private Button resumeButton;
// @FXML
// private Button quitButton;

// private GameScene gameScene;
// private Stage stage;
// private GameManager gameManager;

// public void init(GameScene gameScene, Stage stage, GameManager gameManager) {
// this.gameScene = gameScene;
// this.stage = stage;
// this.gameManager = gameManager;
// }

// @FXML
// public void onNewGameClick(MouseEvent event) {
// soundManager.playSound("Accept.wav");
// int currentLevel = gameManager.getLevelNumber();
// GameScene newScene = new GameScene(stage, stage.getWidth(),
// stage.getHeight(), currentLevel);
// newScene.start();
// stage.setScene(newScene.getScene());
// }

// @FXML
// public void onResumeClick(MouseEvent event) {
// soundManager.playSound("Accept.wav");
// gameManager.resume();
// gameScene.hidePauseOverlay();
// }

// @FXML
// public void onGameSavesClick(MouseEvent event) {
// soundManager.playSound("Accept.wav");
// // Note: SaveLoadScene is now implemented in GameScene.java
// // This old Pause.java may not be in use - consider removing if deprecated
// System.out.println("Game Saves clicked - Use GameScene implementation");
// }

// @FXML
// public void onQuitClick(MouseEvent event) {
// soundManager.playSound("Accept.wav");
// SceneManager.switchTo("mainMenuView");
// }
// }
