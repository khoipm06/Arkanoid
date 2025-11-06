package com.arkanoid.ui.view;

import com.arkanoid.systems.sound.SoundManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainMenuView {
    @FXML
    private Button startGame;
    @FXML
    private Button setting;
    @FXML
    private Button quit;
    @FXML
    private Button shop;
    @FXML
    private Label welcome;
    @FXML
    private Button profile;
    @FXML
    private Button leaderBoard;

    @FXML
    public void initialize() {
        try {
            applyIntroEffect();
            Platform.runLater(() -> applyShineEffect(welcome));
        } catch (Exception e) {
            System.err.println("Không thể tải ảnh nền: background.png");
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

    private void applyShineEffect(Label label) {
        Stop[] stops = new Stop[] {
                new Stop(0, Color.CYAN),
                new Stop(0.5, Color.WHITE),
                new Stop(1, Color.CYAN)
        };

        AnimationTimer timer = new AnimationTimer() {
            double progress = -0.5;

            @Override
            public void handle(long now) {
                progress += 0.01;
                if (progress > 1.5) progress = -0.5;

                LinearGradient dynamic = new LinearGradient(
                        progress, 0, progress + 0.3, 0, true, CycleMethod.NO_CYCLE, stops
                );
                label.setTextFill(dynamic);
            }
        };
        timer.start();
    }

    @FXML
    public void onSettingClick(MouseEvent event) {
        System.out.println("setting");

        SoundManager.playSound("Accept.wav");

        SceneManager.switchTo("settingView");
    }
    @FXML
    public void onQuitClick(MouseEvent event) {

        SoundManager.playSound("Accept.wav");
        System.out.println("Quit");
    }
    @FXML
    public void onShopClick(MouseEvent event) {
        System.out.println("Shop");

        SoundManager.playSound("Accept.wav");

        SceneManager.switchTo("shopView");
    }

    public void onProfileClick(MouseEvent event) {
        System.out.println("Profile");

        SoundManager.playSound("Accept.wav");

        Main.switchToProfileOrAuth();
    }

    @FXML
    private AnchorPane root;

    private Parent modePopup;

    @FXML
    public void onStartGameClick(MouseEvent event) {
        try {
            if (modePopup == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.arkanoid.ui.view/ModeSelectView.fxml"));
                modePopup = loader.load();

                SoundManager.playSound("Accept.wav");

                ModeSelectView popupController = loader.getController();
                popupController.setMainController(this);
                popupController.setStage((Stage) root.getScene().getWindow());

                double rootWidth = root.getPrefWidth();
                double rootHeight = root.getPrefHeight();

                // Lấy kích thước của Pop-up (sau khi tải, nó là AnchorPane)
                AnchorPane popupPane = (AnchorPane)modePopup;
                double popupWidth = popupPane.getPrefWidth();
                double popupHeight = popupPane.getPrefHeight();

                // Tính toán vị trí X và Y để căn giữa
                double centerX = (rootWidth - popupWidth) / 2 + 50;
                double centerY = (rootHeight - popupHeight) / 2 - 30;

                // Thiết lập vị trí
                modePopup.setLayoutX(centerX);
                modePopup.setLayoutY(centerY);
            }

            if (!root.getChildren().contains(modePopup)) {
                root.getChildren().add(modePopup);
            }

            if (!root.getChildren().contains(modePopup)) {
                root.getChildren().add(modePopup);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeModePopup() {

        SoundManager.playSound("Accept.wav");

        root.getChildren().remove(modePopup);
    }

    public void onLeaderBoardClick(MouseEvent event) {

        SoundManager.playSound("Accept.wav");

        System.out.println("leaderBoard");
    }
}
