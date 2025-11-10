package com.arkanoid.ui;

    import com.arkanoid.core.entities.Ball;
    import com.arkanoid.core.entities.Brick;
    import com.arkanoid.core.entities.Explosion;
    import com.arkanoid.core.entities.LineEffect;
    import com.arkanoid.core.entities.Paddle;
    import com.arkanoid.core.entities.PowerUp;
    import com.arkanoid.systems.GameManager;
    import com.arkanoid.ui.view.*;
    import javafx.animation.AnimationTimer;
    import javafx.application.Platform;
    import javafx.fxml.FXMLLoader;
    import javafx.geometry.Pos;
    import javafx.scene.Parent;
    import javafx.scene.Scene;
    import javafx.scene.canvas.Canvas;
    import javafx.scene.canvas.GraphicsContext;
    import javafx.scene.control.Button;
    import javafx.scene.input.KeyCode;
    import javafx.scene.layout.AnchorPane;
    import javafx.scene.layout.StackPane;
    import javafx.scene.layout.VBox;
    import javafx.scene.paint.Color;
    import javafx.scene.text.Font;
    import javafx.scene.text.Text;
    import javafx.stage.Stage;
    import javafx.scene.image.Image;

import java.util.HashSet;
import java.util.Set;

public class GameScene {
    private Scene scene;
    private Stage stage;
    private Canvas canvas;
    private GraphicsContext gc;
    private GameManager gameManager;
    private AnimationTimer gameLoop;
    private Set<KeyCode> pressedKeys;
    private long lastUpdate;
    private VBox pauseOverlay;
    private StackPane root;

    public GameScene(Stage stage, double width, double height) {
        this.stage = stage;
        this.pressedKeys = new HashSet<>();
        this.gameManager = new GameManager(width, height);

        root = new StackPane();
        
        canvas = new Canvas(width, height);
        gc = canvas.getGraphicsContext2D();

        pauseOverlay = createPauseOverlay();
        pauseOverlay.setVisible(false);

        root.getChildren().addAll(canvas, pauseOverlay);
        
        this.scene = new Scene(root, width, height);
        setupInputHandlers();
    }

    private VBox createPauseOverlay() {
        VBox overlay = new VBox(20);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        overlay.setPrefSize(canvas.getWidth(), canvas.getHeight());

        Text pausedText = new Text("PAUSED");
        pausedText.setFont(Font.font("Arial", 50));
        pausedText.setFill(Color.WHITE);

        Text instructionText = new Text("Press ESC to Resume");
        instructionText.setFont(Font.font("Arial", 20));
        instructionText.setFill(Color.LIGHTGRAY);

        overlay.getChildren().addAll(pausedText, instructionText);
        return overlay;
    }

    private void setupInputHandlers() {
        scene.setOnKeyPressed(event -> {
            KeyCode key = event.getCode();
            
            if (key == KeyCode.ESCAPE) {
                gameManager.togglePause();
                pauseOverlay.setVisible(gameManager.getCurrentState() == GameManager.GameState.PAUSED);
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

            renderUI();
        }

        private void renderUI() {
            if (gameManager.getPlayer() == null) return;

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 20));

            String scoreText = "Score: " + gameManager.getPlayer().getState().getScore();
            gc.fillText(scoreText, 10, 25);

            String livesText = "Lives: " + gameManager.getPlayer().getState().getLives();
            gc.fillText(livesText, canvas.getWidth() - 100, 25);
        }

//        private void showGameOver() {
//        }
//
//        private void showLevelComplete() {}


        public Scene getScene() {
            return scene;
        }
    }

    private void update(double deltaTime) {
        gameManager.update(deltaTime);
        
        if (gameManager.getCurrentState() == GameManager.GameState.GAME_OVER) {
            gameLoop.stop();
            showGameOver();
        } else if (gameManager.getCurrentState() == GameManager.GameState.LEVEL_COMPLETE) {
            gameLoop.stop();
            showLevelComplete();
        }
    }

    private void render() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (Brick brick : gameManager.getBricks()) {
            brick.render(gc);
        }

        for (Ball ball : gameManager.getBalls()) {
            ball.render(gc);
        }

        for (PowerUp powerUp : gameManager.getPowerUps()) {
            powerUp.render(gc);
        }

        if (gameManager.getPlayer() != null) {
            gameManager.getPlayer().getPaddle().render(gc);
        }

        renderUI();
    }

    private void renderUI() {
        if (gameManager.getPlayer() == null) return;
        
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 20));
        
        String scoreText = "Score: " + gameManager.getPlayer().getState().getScore();
        gc.fillText(scoreText, 10, 25);
        
        String livesText = "Lives: " + gameManager.getPlayer().getState().getLives();
        gc.fillText(livesText, canvas.getWidth() - 100, 25);
    }

    private void showGameOver() {
        VBox gameOverBox = new VBox(20);
        gameOverBox.setAlignment(Pos.CENTER);
        gameOverBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9);");
        gameOverBox.setPrefSize(canvas.getWidth(), canvas.getHeight());

        Text gameOverText = new Text("GAME OVER");
        gameOverText.setFont(Font.font("Arial", 60));
        gameOverText.setFill(Color.RED);

        Text scoreText = new Text("Final Score: " + gameManager.getPlayer().getState().getScore());
        scoreText.setFont(Font.font("Arial", 30));
        scoreText.setFill(Color.WHITE);

        Text instructionText = new Text("Click to return to menu");
        instructionText.setFont(Font.font("Arial", 20));
        instructionText.setFill(Color.LIGHTGRAY);

        gameOverBox.getChildren().addAll(gameOverText, scoreText, instructionText);
        
        gameOverBox.setOnMouseClicked(e -> {
            MainMenuScene menuScene = new MainMenuScene(stage, canvas.getWidth(), canvas.getHeight());
            stage.setScene(menuScene.getScene());
        });
        
        root.getChildren().add(gameOverBox);
    }

    private void showLevelComplete() {
        VBox completeBox = new VBox(20);
        completeBox.setAlignment(Pos.CENTER);
        completeBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9);");
        completeBox.setPrefSize(canvas.getWidth(), canvas.getHeight());

        Text completeText = new Text("LEVEL COMPLETE!");
        completeText.setFont(Font.font("Arial", 60));
        completeText.setFill(Color.GREEN);

        Text scoreText = new Text("Score: " + gameManager.getPlayer().getState().getScore());
        scoreText.setFont(Font.font("Arial", 30));
        scoreText.setFill(Color.WHITE);

        Text instructionText = new Text("Click to return to menu");
        instructionText.setFont(Font.font("Arial", 20));
        instructionText.setFill(Color.LIGHTGRAY);

        completeBox.getChildren().addAll(completeText, scoreText, instructionText);
        
        completeBox.setOnMouseClicked(e -> {
            MainMenuScene menuScene = new MainMenuScene(stage, canvas.getWidth(), canvas.getHeight());
            stage.setScene(menuScene.getScene());
        });
        
        root.getChildren().add(completeBox);
    }

    public Scene getScene() {
        return scene;
    }
}
