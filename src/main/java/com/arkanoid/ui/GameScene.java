package com.arkanoid.ui;

import com.arkanoid.core.entities.*;
import com.arkanoid.systems.GameManager;
import com.arkanoid.systems.player.PlayerState;
import com.arkanoid.ui.view.SceneManager;
import com.arkanoid.ui.view.SessionManager;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import java.util.HashSet;
import java.util.Set;

public class GameScene {
    private final Scene scene;
    private final Stage stage;
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final GameManager gameManager;
    private AnimationTimer gameLoop;
    private final Set<KeyCode> pressedKeys;
    private long lastUpdate;
    private final VBox pauseOverlay;
    private final StackPane root;
    private Image backgroundImage;

    private Text scoreLabel;
    private Text livesLabel;
    private Text levelLabel;
    private Text timeLabel;
    private long startTime;
    private Text highestscoreLabel;

    public GameScene(Stage stage, double width, double height) {
        this.stage = stage;
        this.pressedKeys = new HashSet<>();
        this.gameManager = new GameManager(width, height);

    public GameScene(Stage stage, double width, double height, int levelNumber) {
        this.stage = stage;
        this.pressedKeys = new HashSet<>();

        double gameAreaWidth = 700;  // cố định để dễ kiểm soát
        double gameAreaHeight = height;

        canvas = new Canvas(gameAreaWidth, gameAreaHeight);
        gc = canvas.getGraphicsContext2D();
        this.gameManager = new GameManager(canvas.getWidth(), canvas.getHeight(), levelNumber);

        if (gameManager.getPlayer() != null) {
            String equippedSkin = SessionManager.getCurrentUser().getEquippedPaddleSkin();
            System.out.println("🎨 Skin paddle đang dùng: " + equippedSkin);
            gameManager.getPlayer().getPaddle().equipSkin(equippedSkin);
        }

        try (InputStream streamBackGround = getClass().getResourceAsStream("/images/backgroundGame.png")) {
            if (streamBackGround != null) {
                this.backgroundImage = new Image(streamBackGround);
            } else {
                System.err.println("Cảnh báo: Không tìm thấy file ảnh background.png. Dùng nền đen.");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh nền: " + e.getMessage());
        }

//            root = new StackPane();
//
//            canvas = new Canvas(width, height);
//            gc = canvas.getGraphicsContext2D();
//
//            pauseOverlay = createPauseOverlay();
//            pauseOverlay.setVisible(false);
//
//            root.getChildren().addAll(canvas, pauseOverlay);
//                this.scene = new Scene(root, width, height);
//                setupInputHandlers();
//                stage.setScene(scene);
        BorderPane mainLayout = new BorderPane();

        // 1. Canvas chơi game (trái)


        // 2. Panel thông tin (phải)
        VBox infoPanel = createInfoPanel();

        // Đặt vào BorderPane
        mainLayout.setCenter(canvas);
        mainLayout.setRight(infoPanel);

        // Pause overlay (đè lên toàn bộ)
        pauseOverlay = createPauseOverlay();
        pauseOverlay.setVisible(false);

        // Root là StackPane để overlay đè lên
        root = new StackPane(mainLayout, pauseOverlay);
        double infoWidth = 350;
        this.scene = new Scene(root, canvas.getWidth() + infoWidth, height);

        setupInputHandlers();
        stage.setResizable(false);
        stage.setScene(scene);

    }

    public void hidePauseOverlay() {
        if (pauseOverlay != null) {
            pauseOverlay.setVisible(false);
        }
    }

    private VBox createInfoPanel() {
        VBox info = new VBox(25);
        info.setAlignment(Pos.TOP_CENTER);
        info.setStyle("-fx-background-color: #1a1a1a; -fx-padding: 30; -fx-border-color: #444; -fx-border-width: 0 0 0 2;");
        info.setPrefWidth(350);

        // Tiêu đề
        Text title = new Text("ARKANOID");
        title.setFont(Font.font("Arial Black", 28));
        title.setFill(Color.CYAN);
        title.setStroke(Color.DARKCYAN);
        title.setStrokeWidth(1);

        // Score
        HBox scoreBox = createInfoRow("SCORE", "0", Color.LIMEGREEN);
        scoreLabel = (Text) scoreBox.getChildren().get(1);

        // LIVES
        HBox livesBox = createInfoRow("LIVES", "3", Color.TOMATO);
        livesLabel = (Text) livesBox.getChildren().get(1);

        // LEVEL
        HBox levelBox = createInfoRow("LEVEL", String.valueOf(gameManager.getLevelNumber()), Color.GOLD);
        levelLabel = (Text) levelBox.getChildren().get(1);

        // ⏱️ TIME
        HBox timeBox = createInfoRow("TIME", "00:00", Color.LIGHTBLUE);
        timeLabel = (Text) timeBox.getChildren().get(1);

        info.getChildren().addAll(title, scoreBox, livesBox, levelBox, timeBox);

        return info;
    }

    private HBox createInfoRow(String label, String value, Color valueColor) {
        Text lbl = new Text(label + ":");
        lbl.setFont(Font.font("Consolas", 18));
        lbl.setFill(Color.LIGHTGRAY);

        Text val = new Text(value);
        val.setFont(Font.font("Consolas", 22));
        val.setFill(valueColor);

        HBox row = new HBox(10, lbl, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox createPauseOverlay() {
        VBox overlay = new VBox(30);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75);"
                + "-fx-padding: 40;"
                + "-fx-border-radius: 20;"
                + "-fx-background-radius: 20;");
        overlay.setPrefSize(canvas.getWidth() * 0.8, canvas.getHeight() * 0.6);

        // Chữ PAUSED đẹp
        Text pausedText = new Text("PAUSED");
        pausedText.setFont(Font.font("Arial Black", 60));
        pausedText.setFill(Color.WHITE);
        pausedText.setStroke(Color.LIGHTBLUE);
        pausedText.setStrokeWidth(2);

        // Nút Resume
        Button resumeBtn = new Button("Resume");
        stylePauseButton(resumeBtn);
        resumeBtn.setOnAction(e -> {
            gameManager.resume();
            pauseOverlay.setVisible(false);
        });

        // Nút New Game
        Button newGameBtn = new Button("New Game");
        stylePauseButton(newGameBtn);
        newGameBtn.setOnAction(e -> {
            GameScene newScene = new GameScene(stage, stage.getWidth(), stage.getHeight(), gameManager.getLevelNumber());
            newScene.start();
            stage.setScene(newScene.getScene());
        });

        // Nút Quit
        Button quitBtn = new Button("Quit");
        stylePauseButton(quitBtn);
        quitBtn.setOnAction(e -> {
            SceneManager.switchTo("mainMenuView");
        });

        overlay.getChildren().addAll(pausedText, resumeBtn, newGameBtn, quitBtn);
        return overlay;
    }

    private void stylePauseButton(Button button) {
        button.setStyle("-fx-background-color: linear-gradient(to bottom right, #1e3c72, #2a5298);"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 20px;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 15;"
                + "-fx-border-radius: 15;"
                + "-fx-border-color: white;"
                + "-fx-border-width: 2;"
                + "-fx-cursor: hand;");
        button.setPrefWidth(200);
        button.setPrefHeight(50);

        // Hiệu ứng hover
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: linear-gradient(to bottom right, #2a5298, #1e3c72);"
                + "-fx-text-fill: yellow;"
                + "-fx-font-size: 22px;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 15;"
                + "-fx-border-radius: 15;"
                + "-fx-border-color: white;"
                + "-fx-border-width: 2;"
                + "-fx-cursor: hand;"));
        button.setOnMouseExited(e -> stylePauseButton(button));
    }

    private void setupInputHandlers() {
        scene.setOnKeyPressed(event -> {
            KeyCode key = event.getCode();

            if (key == KeyCode.ESCAPE) {
                gameManager.pause();
                pauseOverlay.setVisible(gameManager.getCurrentState() == GameManager.GameState.PAUSED);
                return;
            }

            if (key == KeyCode.SPACE) {
                for (Ball ball : gameManager.getBalls()) {
                    if (ball.isAttachedToPaddle()) {
                        ball.launch();
                    }
                }
                return;
            }

            pressedKeys.add(key);
        });

        scene.setOnKeyReleased(event -> {
            pressedKeys.remove(event.getCode());
        });
    }

    public void start() {
        gameManager.startGame();
        lastUpdate = System.nanoTime();
        startTime = System.nanoTime();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                handleInput(deltaTime);
                update(deltaTime);
                render();

            }
        };

        gameLoop.start();
    }

    private void handleInput(double deltaTime) {
        if (gameManager.getCurrentState() != GameManager.GameState.PLAYING) return;

        for (KeyCode key : pressedKeys) {
            gameManager.getPlayerManager().handleInput(key, true, deltaTime);
        }
    }

    private void update(double deltaTime) {
        gameManager.update(deltaTime);
        if (gameManager.getPlayer() != null) {
            PlayerState state = gameManager.getPlayer().getState();
            Platform.runLater(() -> {
                scoreLabel.setText(String.valueOf(state.getScore()));
                livesLabel.setText(String.valueOf(state.getLives()));
            });
        }
        long elapsed = (System.nanoTime() - startTime) / 1_000_000_000;
        int minutes = (int) (elapsed / 60);
        int seconds = (int) (elapsed % 60);
        String timeStr = String.format("%02d:%02d", minutes, seconds);
        Platform.runLater(() -> timeLabel.setText(timeStr));
        if (gameManager.getCurrentState() == GameManager.GameState.GAME_OVER) {
            gameLoop.stop();
            Platform.runLater(() -> {
                int highest = 0;
                SceneManager.showGameOver(
                        gameManager.getLevelNumber(),
                        gameManager.getScore(),
                        highest);
            });
            return;
        }

        if (gameManager.getCurrentState() == GameManager.GameState.LEVEL_COMPLETE) {
            gameLoop.stop();
            Platform.runLater(() -> {
                int highest = 0;
                SceneManager.showWinLevel(
                        gameManager.getLevelNumber(),
                        gameManager.getScore(),
                        highest);
            });
        }
    }

    private void render() {
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, canvas.getWidth(), canvas.getHeight());
        } else {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        }

        for (Brick brick : gameManager.getBricks()) {
            brick.render(gc);
        }

        for (Ball ball : gameManager.getBalls()) {
            ball.render(gc);
        }

        for (PowerUp powerUp : gameManager.getPowerUps()) {
            powerUp.render(gc);
        }
        for (Bullet bullet : gameManager.getBullets()) {
            bullet.render(gc);
        }

        for (Explosion explosion : gameManager.getExplosions()) {
            explosion.render(gc);
        }

        for (LineEffect lineEffect : gameManager.getLineEffects()) {
            lineEffect.render(gc);
        }
        if (gameManager.getPlayer() != null) {
            gameManager.getPlayer().getPaddle().render(gc);
        }

//            renderUI();
    }

//        private void renderUI() {
//            if (gameManager.getPlayer() == null) return;
//
//            gc.setFill(Color.WHITE);
//            gc.setFont(Font.font("Arial", 20));
//
//            String scoreText = "Score: " + gameManager.getPlayer().getState().getScore();
//            gc.fillText(scoreText, 10, 25);
//
//            String livesText = "Lives: " + gameManager.getPlayer().getState().getLives();
//            gc.fillText(livesText, canvas.getWidth() - 100, 25);
//        }

//        private void showGameOver() {
//        }
//
//        private void showLevelComplete() {}


    public Scene getScene() {
        return scene;
    }
}
