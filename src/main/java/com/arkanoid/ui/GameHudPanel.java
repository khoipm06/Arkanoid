package com.arkanoid.ui;

import com.arkanoid.core.GameConstants;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Right-hand HUD panel ({@code 300px} wide) for single-player gameplay.
 * Uses dirty-checking so JavaFX {@link Text} nodes are only updated when values change.
 */
public class GameHudPanel extends VBox {
    private static final String PANEL_STYLE =
            "-fx-background-color: linear-gradient(to bottom, #1a1a2e, #162447); "
                    + "-fx-padding: 30; "
                    + "-fx-border-color: #00ffff; "
                    + "-fx-border-width: 2; "
                    + "-fx-background-radius: 15; "
                    + "-fx-border-radius: 15;";

    private final Text scoreLabel;
    private final Text livesLabel;
    private final Text levelLabel;
    private final Text timeLabel;
    private final HBox fpsBox;
    private final Text fpsLabel;

    private int lastScore = Integer.MIN_VALUE;
    private int lastLives = Integer.MIN_VALUE;
    private int lastLevel = Integer.MIN_VALUE;
    private int lastElapsedSeconds = Integer.MIN_VALUE;
    private String formattedTime = "00:00";

    public GameHudPanel(int initialLevel, boolean showFps) {
        super(20);
        setAlignment(Pos.TOP_CENTER);
        setMinWidth(GameConstants.SINGLE_PLAYER_PANEL_WIDTH);
        setPrefWidth(GameConstants.SINGLE_PLAYER_PANEL_WIDTH);
        setMaxWidth(GameConstants.SINGLE_PLAYER_PANEL_WIDTH);
        setStyle(PANEL_STYLE);

        Text title = new Text("ARKANOID");
        title.setFont(Font.font("Arial Black", 32));
        title.setFill(Color.CYAN);
        title.setStroke(Color.DARKBLUE);
        title.setStrokeWidth(2);

        HBox scoreBox = createInfoRow("SCORE", "0", Color.LIMEGREEN);
        this.scoreLabel = (Text) scoreBox.getChildren().get(1);

        HBox livesBox = createInfoRow("LIVES", "3", Color.TOMATO);
        this.livesLabel = (Text) livesBox.getChildren().get(1);

        HBox levelBox = createInfoRow("LEVEL", String.valueOf(initialLevel), Color.GOLD);
        this.levelLabel = (Text) levelBox.getChildren().get(1);
        this.lastLevel = initialLevel;

        HBox timeBox = createInfoRow("TIME", "00:00", Color.LIGHTBLUE);
        this.timeLabel = (Text) timeBox.getChildren().get(1);

        this.fpsBox = createInfoRow("FPS", "60", Color.ORANGE);
        this.fpsLabel = (Text) fpsBox.getChildren().get(1);

        getChildren().addAll(title, scoreBox, livesBox, levelBox, timeBox);
        if (showFps) {
            getChildren().add(fpsBox);
        }

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 255, 255, 0.6));
        shadow.setRadius(15);
        setEffect(shadow);
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

    /**
     * Updates HUD labels only when score, lives, level, or elapsed seconds change.
     */
    public void updateStats(int score, int lives, int level, int elapsedSeconds) {
        if (score != lastScore) {
            lastScore = score;
            scoreLabel.setText(String.valueOf(score));
        }
        if (lives != lastLives) {
            lastLives = lives;
            livesLabel.setText(String.valueOf(lives));
        }
        if (level != lastLevel) {
            lastLevel = level;
            levelLabel.setText(String.valueOf(level));
        }
        int safeElapsed = Math.max(0, elapsedSeconds);
        if (safeElapsed != lastElapsedSeconds) {
            lastElapsedSeconds = safeElapsed;
            int minutes = safeElapsed / 60;
            int seconds = safeElapsed % 60;
            formattedTime = String.format("%02d:%02d", minutes, seconds);
            timeLabel.setText(formattedTime);
        }
    }

    /**
     * Updates the optional FPS counter when debug logging is active.
     */
    public void updateFps(boolean isDebug, double currentFps, double averageFrameTimeMs) {
        if (isDebug) {
            if (!getChildren().contains(fpsBox)) {
                getChildren().add(fpsBox);
            }
            fpsLabel.setText(String.format("%.0f (%.1fms)", currentFps, averageFrameTimeMs));
            if (currentFps >= 60) {
                fpsLabel.setFill(Color.LIMEGREEN);
            } else if (currentFps >= 45) {
                fpsLabel.setFill(Color.YELLOW);
            } else {
                fpsLabel.setFill(Color.RED);
            }
        } else {
            getChildren().remove(fpsBox);
        }
    }

    public String getFormattedTime() {
        return formattedTime;
    }
}
