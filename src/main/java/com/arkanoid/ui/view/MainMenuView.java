package com.arkanoid.ui.view;

import com.arkanoid.GameApplication;
import com.arkanoid.systems.sound.SoundManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class MainMenuView {
    private final SoundManager soundManager = SoundManager.getInstance();

    @FXML
    private Button startGame;
    @FXML
    private Button setting;
    @FXML
    private Button quit;
    @FXML
    private Button shop;
    @FXML
    private ImageView welcome;
    @FXML
    private Button profile;
    @FXML
    private Button leaderBoard;
    @FXML
    private AnchorPane root;
    private Parent modePopup;

    @FXML
    public void initialize() {
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/arkanoid_intro.png"));
            welcome.setImage(logo);
            applyIntroEffect();
        } catch (Exception e) {
            System.err.println("Cannot load intro image");
            e.printStackTrace();
        }
    }

    @FXML
    private void applyIntroEffect() {
        FadeTransition fade = new FadeTransition(Duration.seconds(2), welcome);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.seconds(2), welcome);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);

        new ParallelTransition(fade, scale).play();
    }

    @FXML
    public void onSettingClick(MouseEvent event) {
        System.out.println("setting");

        soundManager.playSound("Accept.wav");

        SceneManager.switchTo("settingView");
    }

    @FXML
    public void onQuitClick(MouseEvent event) {

        soundManager.playSound("Accept.wav");
        Platform.exit(); // thoát toàn bộ ứng dụng
        System.exit(0);

    }

    @FXML
    public void onShopClick(MouseEvent event) {
        System.out.println("Shop");

        soundManager.playSound("Accept.wav");

        SceneManager.switchTo("shopView");
    }

    public void onProfileClick(MouseEvent event) {
        System.out.println("Profile");

        soundManager.playSound("Accept.wav");

        GameApplication.switchToProfileOrAuth();
    }

    @FXML
    public void onStartGameClick(MouseEvent event) {
        soundManager.playSound("Accept.wav");
        VBox modePopup = createModeSelectPopup();
        root.getChildren().add(modePopup);
    }

    private VBox createModeSelectPopup() {
        VBox overlay = new VBox(30);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);"
                + "-fx-padding: 40;"
                + "-fx-border-radius: 20;"
                + "-fx-background-radius: 20;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 20,0,0,0);");
        overlay.setPrefSize(root.getWidth() * 0.6, root.getHeight() * 0.5);

        // Chữ MODE SELECT
        Text title = new Text("MODE SELECT");
        title.setFont(Font.font("Arial Black", 50));
        title.setFill(Color.WHITE);
        title.setStroke(Color.CYAN);
        title.setStrokeWidth(2);

        // Nút Single Player
        Button singleBtn = new Button("Single Player");
        styleModeSelectButton(singleBtn);
        singleBtn.setOnAction(e -> {
            soundManager.playSound("Accept.wav");
            SceneManager.switchTo("map"); // hoặc start game
            root.getChildren().remove(overlay);
        });

        // Nút Multi Player
        Button multiBtn = new Button("Multi Player");
        styleModeSelectButton(multiBtn);
        multiBtn.setOnAction(e -> {
            soundManager.playSound("Accept.wav");
            root.getChildren().remove(overlay);
            // TwoPlayerGameScreen creates its own Scene programmatically
            TwoPlayerGameScreen twoPlayerScreen = new TwoPlayerGameScreen(SceneManager.getStage());
            twoPlayerScreen.show();
        });

        // Nút Back
        Button backBtn = new Button("Back");
        styleModeSelectButton(backBtn);
        backBtn.setOnAction(e -> {
            soundManager.playSound("Accept.wav");
            root.getChildren().remove(overlay);
        });

        overlay.getChildren().addAll(title, singleBtn, multiBtn, backBtn);

        // Căn giữa overlay
        Platform.runLater(() -> {
            overlay.setLayoutX((root.getWidth() - overlay.getPrefWidth()) / 2);
            overlay.setLayoutY((root.getHeight() - overlay.getPrefHeight()) / 2);
        });

        return overlay;
    }

    private void styleModeSelectButton(Button button) {
        String normalStyle = "-fx-background-color: linear-gradient(to bottom right, #1e3c72, #2a5298);"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 20px;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 15;"
                + "-fx-border-radius: 15;"
                + "-fx-border-color: white;"
                + "-fx-border-width: 2;"
                + "-fx-cursor: hand;";

        String hoverStyle = "-fx-background-color: linear-gradient(to bottom right, #2a5298, #1e3c72);"
                + "-fx-text-fill: yellow;"
                + "-fx-font-size: 22px;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 15;"
                + "-fx-border-radius: 15;"
                + "-fx-border-color: white;"
                + "-fx-border-width: 2;"
                + "-fx-cursor: hand;";

        button.setStyle(normalStyle);
        button.setPrefWidth(200);
        button.setPrefHeight(50);

        // Hiệu ứng hover
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));
    }

    public void closeModePopup() {

        soundManager.playSound("Accept.wav");

        root.getChildren().remove(modePopup);
    }

    public void onLeaderBoardClick(MouseEvent event) {

        soundManager.playSound("Accept.wav");

        SceneManager.switchTo("leaderboard");
    }
}
