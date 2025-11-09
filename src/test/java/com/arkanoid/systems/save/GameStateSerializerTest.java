package com.arkanoid.systems.save;

import com.arkanoid.systems.save.impl.GameStateSerializerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GameStateSerializer implementation.
 */
public class GameStateSerializerTest {

    private GameStateSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new GameStateSerializerImpl();
    }

    @Test
    void testSerializeAndDeserializeMinimalGameState() {
        // Create minimal game state
        PaddleState paddleState = new PaddleState(400, 550, 100, 20, 0, null, null, 0);
        List<BallState> ballStates = List.of(
                new BallState(400, 300, 5, -5, 10, false, null));
        List<BrickState> brickStates = new ArrayList<>();
        List<PowerUpState> powerUps = new ArrayList<>();

        GameState originalState = new GameState();
        originalState.setLevelNumber(1);
        originalState.setScore(1000);
        originalState.setLives(3);
        originalState.setElapsedTimeSeconds(60);
        originalState.setPaddleState(paddleState);
        originalState.setBallStates(ballStates);
        originalState.setBrickStates(brickStates);
        originalState.setActivePowerUps(powerUps);

        // Serialize
        String json = serializer.toJson(originalState);
        System.out.println("Generated JSON:");
        System.out.println(json);
        assertNotNull(json);
        assertFalse(json.isEmpty());
        assertTrue(json.contains("\"levelNumber\":1") || json.contains("\"levelNumber\": 1"));
        assertTrue(json.contains("\"score\":1000") || json.contains("\"score\": 1000"));
        assertTrue(json.contains("\"lives\":3") || json.contains("\"lives\": 3"));

        // Deserialize
        GameState deserializedState = serializer.fromJson(json);
        assertNotNull(deserializedState);
        assertEquals(1, deserializedState.getLevelNumber());
        assertEquals(1000, deserializedState.getScore());
        assertEquals(3, deserializedState.getLives());
        assertEquals(60, deserializedState.getElapsedTimeSeconds());

        // Verify paddle state
        PaddleState deserializedPaddle = deserializedState.getPaddleState();
        assertNotNull(deserializedPaddle);
        assertEquals(400, deserializedPaddle.x());
        assertEquals(550, deserializedPaddle.y());
        assertEquals(100, deserializedPaddle.width());
        assertEquals(20, deserializedPaddle.height());

        // Verify ball states
        assertEquals(1, deserializedState.getBallStates().size());
        BallState deserializedBall = deserializedState.getBallStates().get(0);
        assertEquals(400, deserializedBall.x());
        assertEquals(300, deserializedBall.y());
        assertEquals(5, deserializedBall.velocityX());
        assertEquals(-5, deserializedBall.velocityY());
        assertEquals(10, deserializedBall.radius());
        assertFalse(deserializedBall.attachedToPaddle());
    }

    @Test
    void testIsValidGameState() {
        // Valid state
        PaddleState paddleState = new PaddleState(400, 550, 100, 20, 0, null, null, 0);
        List<BallState> ballStates = List.of(
                new BallState(400, 300, 5, -5, 10, false, null));
        GameState validState = new GameState();
        validState.setLevelNumber(1);
        validState.setScore(0);
        validState.setLives(3);
        validState.setElapsedTimeSeconds(0);
        validState.setPaddleState(paddleState);
        validState.setBallStates(ballStates);
        validState.setBrickStates(new ArrayList<>());
        validState.setActivePowerUps(new ArrayList<>());

        assertTrue(serializer.isValidGameState(validState));

        // Invalid state - no balls
        GameState invalidState = new GameState();
        invalidState.setLevelNumber(1);
        invalidState.setScore(0);
        invalidState.setLives(3);
        invalidState.setElapsedTimeSeconds(0);
        invalidState.setPaddleState(paddleState);
        invalidState.setBallStates(new ArrayList<>());
        invalidState.setBrickStates(new ArrayList<>());
        invalidState.setActivePowerUps(new ArrayList<>());

        assertFalse(serializer.isValidGameState(invalidState));
    }

    @Test
    void testExtractMetadata() {
        PaddleState paddleState = new PaddleState(400, 550, 100, 20, 0, null, null, 0);
        List<BallState> ballStates = List.of(
                new BallState(400, 300, 5, -5, 10, false, null));
        GameState state = new GameState();
        state.setLevelNumber(3);
        state.setScore(5000);
        state.setLives(2);
        state.setElapsedTimeSeconds(120);
        state.setPaddleState(paddleState);
        state.setBallStates(ballStates);
        state.setBrickStates(new ArrayList<>());
        state.setActivePowerUps(new ArrayList<>());

        String json = serializer.toJson(state);
        GameStateSerializer.GameStateMetadata metadata = serializer.extractMetadata(json);

        assertNotNull(metadata);
        assertEquals(3, metadata.levelNumber());
        assertEquals(5000, metadata.score());
        assertEquals(2, metadata.lives());
    }

    @Test
    void testIsValidJson() {
        String validJson = "{\"levelNumber\":1,\"score\":1000,\"lives\":3,\"paddleState\":{},\"ballStates\":[],\"brickStates\":[]}";
        assertTrue(serializer.isValidJson(validJson));

        String invalidJson = "{invalid json}";
        assertFalse(serializer.isValidJson(invalidJson));

        String emptyJson = "";
        assertFalse(serializer.isValidJson(emptyJson));

        assertFalse(serializer.isValidJson(null));
    }
}
