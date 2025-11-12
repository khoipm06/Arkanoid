package com.arkanoid.ui.view;

import com.arkanoid.GameApplication;
import com.arkanoid.systems.logging.GameLogger;
import com.arkanoid.systems.sound.SoundManager;
import com.arkanoid.ui.components.ToastNotification;
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
import org.slf4j.Logger;

public class MainMenuView {
    private static final Logger logger = GameLogger.getLogger(MainMenuView.class);
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
            logger.error("Cannot load intro image", e);
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
        logger.debug("Settings button clicked");

        soundManager.playSound("Accept.wav");

        SceneManager.switchTo("settingView");
    }

    @FXML
    public void onQuitClick(MouseEvent event) {

        soundManager.playSound("Accept.wav");
        Platform.exit();
        System.exit(0);

    }

    @FXML
    public void onShopClick(MouseEvent event) {
        logger.debug("Shop button clicked");

        soundManager.playSound("Accept.wav");

        if (SessionManager.getCurrentUser() == null) {
            String message = "Please log in to access the shop!";
            ToastNotification.showToast(message, root, ToastNotification.ToastType.ERROR);
            logger.warn(message);
            return;
        }

        SceneManager.switchTo("shopView");
    }

    public void onProfileClick(MouseEvent event) {
        logger.debug("Profile button clicked");

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
        overlay.setStyle("-fx-background-color: linear-gradient(to bottom, rgba(10, 20, 35, 0.95), rgba(5, 10, 20, 0.95));"
                + "-fx-padding: 40;"
                + "-fx-border-radius: 20;"
                + "-fx-background-radius: 20;"
                + "-fx-border-color: linear-gradient(to right, #00d4ff, #0080ff, #00d4ff);"
                + "-fx-border-width: 3;"
                + "-fx-effect: dropshadow(gaussian, rgba(0, 212, 255, 0.7), 30, 0.6, 0, 0);");
        
        double overlayWidth = 450;
        double overlayHeight = 400;
        overlay.setMinSize(overlayWidth, overlayHeight);
        overlay.setMaxSize(overlayWidth, overlayHeight);
        overlay.setPrefSize(overlayWidth, overlayHeight);

        Text title = new Text("MODE SELECT");
        title.setFont(Font.font("Arial Black", 50));
        title.setFill(Color.WHITE);
        title.setStroke(Color.CYAN);
        title.setStrokeWidth(2);
        title.setStyle("-fx-effect: dropshadow(gaussian, rgba(0, 212, 255, 0.8), 15, 0.7, 0, 0);");

        Button singleBtn = new Button("Single Player");
        styleModeSelectButton(singleBtn);
        singleBtn.setOnAction(e -> {
            soundManager.playSound("Accept.wav");
            if (SessionManager.getCurrentUser() == null) {
                root.getChildren().remove(overlay);
                String message = "Please log in to play Single Player mode!";
                ToastNotification.showToast(message, root, ToastNotification.ToastType.ERROR);
                logger.warn(message);
                return;
            }
            SceneManager.switchTo("map");
            root.getChildren().remove(overlay);
        });

        Button multiBtn = new Button("Multi Player");
        styleModeSelectButton(multiBtn);
        multiBtn.setOnAction(e -> {
            soundManager.playSound("Accept.wav");
            root.getChildren().remove(overlay);
            TwoPlayerGameScreen twoPlayerScreen = new TwoPlayerGameScreen(SceneManager.getStage());
            twoPlayerScreen.show();
        });

        Button backBtn = new Button("Back");
        styleModeSelectButton(backBtn);
        backBtn.setOnAction(e -> {
            soundManager.playSound("Accept.wav");
            root.getChildren().remove(overlay);
        });

        overlay.getChildren().addAll(title, singleBtn, multiBtn, backBtn);

        // Center the overlay
        AnchorPane.setTopAnchor(overlay, (root.getHeight() - overlayHeight) / 2);
        AnchorPane.setLeftAnchor(overlay, (root.getWidth() - overlayWidth) / 2);

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

        button.setOnMouseEntered(e -> {
            button.setStyle(hoverStyle);
            button.setScaleX(1.05);
            button.setScaleY(1.05);
        });
        button.setOnMouseExited(e -> {
            button.setStyle(normalStyle);
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
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
