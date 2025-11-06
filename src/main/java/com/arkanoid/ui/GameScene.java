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


    import java.io.IOException;
    import java.io.InputStream;
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
        private Image backgroundImage;


        public GameScene(Stage stage, double width, double height, int levelNumber) {
            this.stage = stage;
            this.pressedKeys = new HashSet<>();

            this.gameManager = new GameManager(width, height, levelNumber);
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

//            InputStream stream = getClass().getResourceAsStream("/images/paddle_Default.png");
//            if (stream == null) {
//                System.out.println("Không tìm thấy file ảnh!");
//            } else {
//                Image paddleImg = new Image(stream);
//                // Sau khi Player được khởi tạo trong GameManager, dòng này sẽ chạy thành công
//                if (gameManager.getPlayer() != null) {
//                    gameManager.getPlayer().getPaddle().setPaddleImage(paddleImg);
//                } else {
//                    // Dòng này sẽ không in ra nữa nếu bạn đã sửa GameManager
//                    System.out.println("Lỗi: Player chưa được khởi tạo!");
//                }
//            }

            root = new StackPane();

            canvas = new Canvas(width, height);
            gc = canvas.getGraphicsContext2D();

            pauseOverlay = createPauseOverlay();
            pauseOverlay.setVisible(false);

            root.getChildren().addAll(canvas, pauseOverlay);

            this.scene = new Scene(root, width, height);
            setupInputHandlers();
            stage.setScene(scene);

        }
        public void hidePauseOverlay() {
            if (pauseOverlay != null) {
                pauseOverlay.setVisible(false);
            }
        }

        private void showPauseScene() {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Pause.fxml"));
                AnchorPane pauseRoot = loader.load();

                Pause pauseController = loader.getController();
                pauseController.init(this, stage, gameManager);

                Scene pauseScene = new Scene(pauseRoot, stage.getWidth(), stage.getHeight());
                stage.setScene(pauseScene);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private VBox createPauseOverlay() {
//            VBox overlay = new VBox(20);
//            overlay.setAlignment(Pos.CENTER);
//            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
//            overlay.setPrefSize(canvas.getWidth(), canvas.getHeight());
//
//            Text pausedText = new Text("PAUSED");
//            pausedText.setFont(Font.font("Arial", 50));
//            pausedText.setFill(Color.WHITE);
//
//            Button resumeBtn = new Button("Resume");
//            resumeBtn.setOnAction(e -> {
//                gameManager.resume();
//                pauseOverlay.setVisible(false);
//            });
//
//            Button newGameBtn = new Button("New Game");
//            newGameBtn.setOnAction(e -> {
//                GameScene newScene = new GameScene(stage, stage.getWidth(), stage.getHeight(), gameManager.getLevelNumber());
//                newScene.start();
//                stage.setScene(newScene.getScene());
//            });
//
//            Button quitBtn = new Button("Quit");
//            quitBtn.setOnAction(e -> {
//                SceneManager.switchTo("mainMenuView");
//            });
//
//            overlay.getChildren().addAll(pausedText, resumeBtn, newGameBtn, quitBtn);
//            return overlay;
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

//            if (gameManager.getCurrentState() == GameManager.GameState.GAME_OVER) {
//                gameLoop.stop();
//                showGameOver();
//            } else if (gameManager.getCurrentState() == GameManager.GameState.LEVEL_COMPLETE) {
//                gameLoop.stop();
//                showLevelComplete();
//            }
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
                return;
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
