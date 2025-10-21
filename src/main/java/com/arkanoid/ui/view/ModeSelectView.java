    package com.arkanoid.ui.view;


    import com.arkanoid.GameApplication;
    import com.arkanoid.systems.sound.SoundManager;
    import com.arkanoid.ui.GameScene;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.scene.Parent;
    import javafx.scene.Scene;
    import javafx.scene.Node;
    import javafx.scene.control.Button;
    import javafx.scene.input.MouseEvent;
    import javafx.scene.layout.AnchorPane;
    import javafx.stage.Stage;

    import java.io.IOException;

    public class ModeSelectView {
        @FXML
        private Button singlePlayer;
        @FXML
        private Button multiPlayer;
        @FXML
        private Button back;
        @FXML
        private AnchorPane root;

        private MainMenuView mainController;
        private Stage stage;

        public void setStage(Stage stage) {
            this.stage = stage;
        }

        @FXML
        private void onSinglePlayerClick(MouseEvent event) throws IOException {
            SoundManager.playSound("Accept.wav");
            SceneManager.switchTo("map");
        }

        @FXML
        public void onMultiPlayerClick(MouseEvent event) {
            System.out.println("Multi Player");

            SoundManager.playSound("Accept.wav");

            SceneManager.switchTo("multiPlayerScene");
        }
        @FXML
        public void onBackClick(MouseEvent event) {
            System.out.println("Quay lại menu chính");

            SoundManager.playSound("Accept.wav");

            if (mainController != null) {
                mainController.closeModePopup();
            }

        }

        public void setMainController(MainMenuView controller) {
            this.mainController = controller;
        }
    }
