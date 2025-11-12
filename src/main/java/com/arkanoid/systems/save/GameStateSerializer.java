package com.arkanoid.systems.save;

import com.google.gson.JsonSyntaxException;

/**
 * Contract for serializing and deserializing game state to/from JSON.
 * 
 * <p>
 * This interface abstracts the JSON serialization logic from the save/load
 * system, allowing for easier testing and potential format changes.
 * 
 * <p>
 * <b>Implementation Requirements:</b>
 * <ul>
 * <li>Must use Gson library for serialization</li>
 * <li>Must handle nested objects (BallState, BrickState, etc.) correctly</li>
 * <li>Must preserve precision for double values (positions, velocities)</li>
 * <li>Must be deterministic (same state → same JSON)</li>
 * <li>Should include version field for future compatibility</li>
 * </ul>
 * 
 * <p>
 * <b>JSON Format:</b> Compact representation (no pretty-printing) to minimize
 * database storage. Expected size: 5-50KB depending on level complexity.
 * 
 * <p>
 * <b>Error Handling:</b>
 * <ul>
 * <li>toJson(): Never throws (logs error and returns error JSON)</li>
 * <li>fromJson(): Throws JsonSyntaxException if JSON is malformed</li>
 * <li>fromJson(): Throws IllegalStateException if state is invalid after
 * parsing</li>
 * </ul>
 * 
 * <p>
 * <b>Thread Safety:</b> Implementations must be stateless and thread-safe.
 * Multiple threads may call toJson()/fromJson() concurrently.
 * 
 * @author Game Save System Specification
 * @version 1.0
 * @see GameState
 */
public interface GameStateSerializer {

    /**
     * Current serialization format version.
     * Increment when GameState structure changes incompatibly.
     */
    String CURRENT_VERSION = "1.0";

    /**
     * Maximum allowed JSON size (1MB safety limit).
     * Prevents database bloat and OOM errors.
     */
    int MAX_JSON_SIZE_BYTES = 1_048_576; // 1MB

    // ==================== Serialization ====================

    /**
     * Converts a GameState object to compact JSON string.
     * 
     * <p>
     * <b>Output format:</b>
     * 
     * <pre>
     * {
     *   "version":"1.0",
     *   "levelNumber":2,
     *   "score":15420,
     *   "lives":3,
     *   "elapsedTimeSeconds":145,
     *   "paddleState":{...},
     *   "ballStates":[...],
     *   "brickStates":[...],
     *   "activePowerUps":[...]
     * }
     * </pre>
     * 
     * <p>
     * <b>Process:</b>
     * <ol>
     * <li>Create Gson instance with custom adapters if needed</li>
     * <li>Add "version" field to root object</li>
     * <li>Serialize GameState using Gson.toJson()</li>
     * <li>Validate output size &lt; MAX_JSON_SIZE_BYTES</li>
     * </ol>
     * 
     * <p>
     * <b>Performance:</b> Target &lt;100ms for typical game state (50 bricks).
     * 
     * @param gameState the in-memory game state to serialize
     * @return compact JSON string representation
     * @throws IllegalArgumentException if gameState is null
     * @throws IllegalStateException    if serialized JSON exceeds
     *                                  MAX_JSON_SIZE_BYTES
     */
    String toJson(GameState gameState);

    // ==================== Deserialization ====================

    /**
     * Converts a JSON string back to a GameState object.
     * 
     * <p>
     * <b>Process:</b>
     * <ol>
     * <li>Parse JSON to JsonObject</li>
     * <li>Check "version" field:
     * <ul>
     * <li>If missing or &lt; CURRENT_VERSION: apply migration logic</li>
     * <li>If &gt; CURRENT_VERSION: throw IllegalStateException</li>
     * </ul>
     * </li>
     * <li>Deserialize using Gson.fromJson()</li>
     * <li>Validate deserialized state:
     * <ul>
     * <li>All required fields present</li>
     * <li>Lists not null (use empty list if needed)</li>
     * <li>Positions within valid ranges</li>
     * <li>At least one ball exists</li>
     * </ul>
     * </li>
     * </ol>
     * 
     * <p>
     * <b>Migration Example</b> (v1.0 → v1.1 adds "difficulty" field):
     * 
     * <pre>
     * if (!json.has("difficulty")) {
     *     json.addProperty("difficulty", "normal");
     * }
     * </pre>
     * 
     * <p>
     * <b>Performance:</b> Target &lt;50ms for typical game state.
     * 
     * @param json the JSON string to deserialize
     * @return fully populated GameState object
     * @throws IllegalArgumentException      if json is null or empty
     * @throws JsonSyntaxException           if JSON is malformed or invalid
     * @throws IllegalStateException         if deserialized state is invalid
     *                                       (missing required data)
     * @throws UnsupportedOperationException if version &gt; CURRENT_VERSION
     *                                       (incompatible)
     */
    GameState fromJson(String json);

    // ==================== Validation ====================

    /**
     * Validates a JSON string without fully deserializing it.
     * Used for quick sanity checks before attempting full load.
     * 
     * <p>
     * Checks:
     * <ul>
     * <li>Valid JSON syntax</li>
     * <li>Contains required root fields (levelNumber, score, lives, etc.)</li>
     * <li>Size within limits</li>
     * </ul>
     * 
     * @param json the JSON string to validate
     * @return true if JSON appears valid, false otherwise
     */
    boolean isValidJson(String json);

    /**
     * Validates a GameState object before serialization.
     * 
     * <p>
     * Checks:
     * <ul>
     * <li>All required fields not null</li>
     * <li>At least one ball exists</li>
     * <li>At least one brick exists (unless level complete)</li>
     * <li>Positions within canvas bounds (0-800 x, 0-600 y typical)</li>
     * <li>Velocities are finite numbers</li>
     * </ul>
     * 
     * @param gameState the state object to validate
     * @return true if state is valid, false otherwise
     */
    boolean isValidGameState(GameState gameState);

    // ==================== Utility ====================

    /**
     * Extracts metadata from a JSON string without full deserialization.
     * Used for quick ListView display without parsing entire state.
     * 
     * <p>
     * Extracted fields:
     * <ul>
     * <li>levelNumber</li>
     * <li>score</li>
     * <li>lives</li>
     * </ul>
     * 
     * @param json the JSON string
     * @return metadata object with key fields, or null if parsing fails
     */
    GameStateMetadata extractMetadata(String json);

    /**
     * Lightweight metadata extracted from JSON for UI display.
     */
    record GameStateMetadata(int levelNumber, int score, int lives) {
    }
}
