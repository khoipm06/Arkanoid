    package com.arkanoid.ui;

    import com.arkanoid.core.entities.Ball;
    import com.arkanoid.core.entities.Brick;
    import com.arkanoid.core.entities.Paddle;
    import com.arkanoid.core.entities.PowerUp;
    import com.arkanoid.systems.GameManager;
    import javafx.animation.AnimationTimer;
    import javafx.geometry.Pos;
    import javafx.scene.Scene;
    import javafx.scene.canvas.Canvas;
    import javafx.scene.canvas.GraphicsContext;
    import javafx.scene.input.KeyCode;
    import javafx.scene.layout.StackPane;
    import javafx.scene.layout.VBox;
    import javafx.scene.paint.Color;
    import javafx.scene.text.Font;
    import javafx.scene.text.Text;
    import javafx.stage.Stage;
    import javafx.scene.image.Image;


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

            try (InputStream streamBackGround = getClass().getResourceAsStream("/images/backgroundGame.png")) {
                if (streamBackGround != null) {
                    this.backgroundImage = new Image(streamBackGround);
                } else {
                    System.err.println("Cảnh báo: Không tìm thấy file ảnh background.png. Dùng nền đen.");
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi tải ảnh nền: " + e.getMessage());
            }

            InputStream stream = getClass().getResourceAsStream("/images/p101.png");
            if (stream == null) {
                System.out.println("Không tìm thấy file ảnh!");
            } else {
                Image paddleImg = new Image(stream);
                // Sau khi Player được khởi tạo trong GameManager, dòng này sẽ chạy thành công
                if (gameManager.getPlayer() != null) {
                    gameManager.getPlayer().getPaddle().setPaddleImage(paddleImg);
                } else {
                    // Dòng này sẽ không in ra nữa nếu bạn đã sửa GameManager
                    System.out.println("Lỗi: Player chưa được khởi tạo!");
                }
            }

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

            if (gameManager.getCurrentState() == GameManager.GameState.GAME_OVER) {
                gameLoop.stop();
                showGameOver();
            } else if (gameManager.getCurrentState() == GameManager.GameState.LEVEL_COMPLETE) {
                gameLoop.stop();
                showLevelComplete();
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
        }

        private void showLevelComplete() {}

        public Scene getScene() {
            return scene;
        }
    }
