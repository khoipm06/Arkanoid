package com.arkanoid.ui.view;

import com.arkanoid.systems.sound.SoundManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;

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
    private ImageView welcome;
    @FXML
    private Button profile;
    @FXML
    private Button leaderBoard;

    @FXML
    public void initialize() {
        try {
            Image logo = new Image(getClass().getResourceAsStream("/images/arkanoid.png"));
            welcome.setImage(logo);

            applyIntroEffect();
//            applyGlowEffect(welcome);// fade + scale vẫn dùng được
        } catch (Exception e) {
            System.err.println("Không thể tải ảnh logo");
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

//    private void applyShineEffect(Label label) {
//        Stop[] stops = new Stop[] {
//                new Stop(0, Color.CYAN),
//                new Stop(0.5, Color.WHITE),
//                new Stop(1, Color.CYAN)
//        };
//
//        AnimationTimer timer = new AnimationTimer() {
//            double progress = -0.5;
//
//            @Override
//            public void handle(long now) {
//                progress += 0.01;
//                if (progress > 1.5) progress = -0.5;
//
//                LinearGradient dynamic = new LinearGradient(
//                        progress, 0, progress + 0.3, 0, true, CycleMethod.NO_CYCLE, stops
//                );
//                label.setTextFill(dynamic);
//            }
//        };
//        timer.start();
//    }
    private void applyGlowEffect(ImageView img) {
        Glow glow = new Glow(0.3);
        img.setEffect(glow);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.2)),
                new KeyFrame(Duration.seconds(1.5), new KeyValue(glow.levelProperty(), 1.0))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.setAutoReverse(true);
        timeline.play();
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
        Platform.exit(); // thoát toàn bộ ứng dụng
        System.exit(0);    }
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
//        try {
//            if (modePopup == null) {
//                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.arkanoid.ui.view/ModeSelectView.fxml"));
//                modePopup = loader.load();
//
//                SoundManager.playSound("Accept.wav");
//
//                ModeSelectView popupController = loader.getController();
//                popupController.setMainController(this);
//                popupController.setStage((Stage) root.getScene().getWindow());
//
//                double rootWidth = root.getPrefWidth();
//                double rootHeight = root.getPrefHeight();
//
//                // Lấy kích thước của Pop-up (sau khi tải, nó là AnchorPane)
//                AnchorPane popupPane = (AnchorPane)modePopup;
//                double popupWidth = popupPane.getPrefWidth();
//                double popupHeight = popupPane.getPrefHeight();
//
//                // Tính toán vị trí X và Y để căn giữa
//                double centerX = (rootWidth - popupWidth) / 2 + 50;
//                double centerY = (rootHeight - popupHeight) / 2 - 30;
//
//                // Thiết lập vị trí
//                modePopup.setLayoutX(centerX);
//                modePopup.setLayoutY(centerY);
//            }
//
//            if (!root.getChildren().contains(modePopup)) {
//                root.getChildren().add(modePopup);
//            }
//
//            if (!root.getChildren().contains(modePopup)) {
//                root.getChildren().add(modePopup);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        SoundManager.playSound("Accept.wav");
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
            SoundManager.playSound("Accept.wav");
            SceneManager.switchTo("map"); // hoặc start game
            root.getChildren().remove(overlay);
        });

        // Nút Multi Player
        Button multiBtn = new Button("Multi Player");
        styleModeSelectButton(multiBtn);
        multiBtn.setOnAction(e -> {
            SoundManager.playSound("Accept.wav");
            SceneManager.switchTo("multiPlayerScene");
            root.getChildren().remove(overlay);
        });

        // Nút Back
        Button backBtn = new Button("Back");
        styleModeSelectButton(backBtn);
        backBtn.setOnAction(e -> {
            SoundManager.playSound("Accept.wav");
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

        SoundManager.playSound("Accept.wav");

        root.getChildren().remove(modePopup);
    }

    public void onLeaderBoardClick(MouseEvent event) {

        SoundManager.playSound("Accept.wav");

        System.out.println("leaderBoard");
    }
}
