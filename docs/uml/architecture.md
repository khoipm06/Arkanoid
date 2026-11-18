classDiagram
direction BT
class AuthScreen {
  + AuthScreen() 
  + onSignInButtonClick() void
  + onBackClick(MouseEvent) void
  + onSignUpButtonClick(MouseEvent) void
}
class Ball {
  + Ball(double, double, double, double) 
  + reset(double, double) void
  + setBounds(double, double, double, double) void
  + launch() void
  + equipSkin(String) void
  - checkWallCollision() void
  + checkPaddleCollision(Paddle) void
  + hasExploded() boolean
  + render(GraphicsContext) void
  + getSkin(String) Image
  + getEquippedSkin() String
  + update(double) void
   String equippedSkin
   Color color
   boolean explosive
   boolean hasExploded
   boolean attachedToPaddle
   Image ballImage
   double radius
   boolean topIsDeadSide
   boolean outOfBounds
}
class BallState {
  + BallState(double, double, double, double, double, boolean, String) 
  + y() double
  + velocityY() double
  + attachedToPaddle() boolean
  + x() double
  + velocityX() double
  + radius() double
  + skin() String
}
class BaseBrick {
  + BaseBrick(double, double, double, double, int, Color, int, int) 
  + BaseBrick(double, double, double, double, int, Color, int, int, String) 
  + destroy() void
  + render(GraphicsContext) void
  + hit() void
  + instantDestroy() void
  + dropPowerUp() PowerUp
  + update(double) void
  - createRandomPowerUp() PowerUp
   boolean destroyed
   String texturePath
   Color color
   int row
   int col
}
class BaseInputHandler {
  + BaseInputHandler() 
  + handleContinuousInput(Set~KeyCode~, double) void
  # isSpaceKey(KeyCode) boolean
  # launchBall(Ball) boolean
  # isPauseKey(KeyCode) boolean
  + handleKeyPress(KeyCode) boolean
}
class Brick {
<<Interface>>
  + hit() void
  + destroy() void
  + intersects(GameObject) boolean
  + update(double) void
  + render(GraphicsContext) void
  + dropPowerUp() PowerUp
  + instantDestroy() void
   double width
   Color color
   int row
   double height
   double x
   double y
   boolean destroyed
   double centerY
   double centerX
   int col
}
class BrickState {
  + BrickState(String, double, double, double, double, int, int, boolean, double, double, String) 
  + y() double
  + visible() boolean
  + width() double
  + x() double
  + velocityY() double
  + height() double
  + hitPointsRemaining() int
  + velocityX() double
  + texturePath() String
  + type() String
  + colorIndex() int
}
class Bullet {
  + Bullet(double, double, double, double, double) 
  + isOutOfBounds(double) boolean
  + update(double) void
  + render(GraphicsContext) void
  + reset(double, double, double, double, double) void
}
class CollisionDetector {
  + CollisionDetector() 
  - handleBallBrickCollision(Ball, Brick) void
  + checkBallBrickCollisions(Ball, List~Brick~, CollisionCallback, GameManager) void
}
class CollisionService {
<<Interface>>
  + resolveBallBallCollision(Ball, Ball) void
  + checkBallBallCollision(Ball, Ball) boolean
  + checkOpponentPaddleHit(Ball, int) boolean
}
class CollisionServiceImpl {
  + CollisionServiceImpl(Player, Player) 
  + resolveBallBallCollision(Ball, Ball) void
  + checkBallBallCollision(Ball, Ball) boolean
  + checkOpponentPaddleHit(Ball, int) boolean
}
class ColorCache {
  + ColorCache() 
  + clear() void
  + getWithAlpha(Color, double) Color
  + getColor(double, double, double, double) Color
}
class CommandLineArgs {
  + CommandLineArgs() 
  + printUsage() void
  + parse(String[]) Config
}
class CompressionUtil {
  + CompressionUtil() 
  + compress(String) byte[]
  - decompressFrom(InputStream) String
  - compressTo(String, OutputStream) void
  + compressToFile(String, File) void
  + decompressFromFile(File) String
  + decompress(byte[]) String
}
class DatabaseException {
  + DatabaseException(String) 
  + DatabaseException(String, Throwable) 
}
class DatabaseManager {
  - DatabaseManager() 
  + executeUpdate(Consumer~Connection~) void
  - createTables(Connection) void
  + executeInTransaction(Function~Connection, T~) T
  + executeQuery(Function~Connection, T~) T
  + initialize() void
  + close() void
  + releaseConnection(Connection) void
   String poolStats
   Connection connection
   DatabaseManager instance
   long activeConnectionCount
}
class DuplicateEntityException {
  + DuplicateEntityException(String) 
}
class EntityFactory {
  + EntityFactory() 
  + createPowerUp(String, double, double) PowerUp
  + createBrick(JsonObject, int, int) Brick
}
class EntityNotFoundException {
  + EntityNotFoundException(String) 
}
class ExpandPaddlePowerUp {
  + ExpandPaddlePowerUp(double, double) 
  + applyEffect(Paddle) void
  + removeEffect(Paddle) void
}
class Explosion {
  + Explosion(double, double, double, double, double) 
  + update(double) void
  + render(GraphicsContext) void
}
class ExplosiveBallPowerUp {
  + ExplosiveBallPowerUp(double, double) 
  + removeEffect(Paddle) void
  + applyEffect(Paddle) void
}
class FloatingText {
  + FloatingText(String, double, double, double, double, Color) 
  + reset(String, double, double, double, double, Color) void
  + update(double) void
  + render(GraphicsContext) void
   boolean active
}
class GameApplication {
  + GameApplication() 
  + switchToProfileOrAuth() void
  + start(Stage) void
  + main(String[]) void
}
class GameConstants {
  - GameConstants() 
   double WINDOW_WIDTH$
   double WINDOW_HEIGHT$
   double SINGLE_PLAYER_CANVAS_WIDTH$
   double SINGLE_PLAYER_PANEL_WIDTH$
   double TWO_PLAYER_DESIGN_WIDTH$
   double TWO_PLAYER_CANVAS_WIDTH$
   double TWO_PLAYER_CANVAS_HEIGHT$
   double TWO_PLAYER_PANEL_WIDTH$
   double DEFAULT_DESIGN_WIDTH$
   double DEFAULT_DESIGN_HEIGHT$
   double MAX_FRAME_DELTA$
   double MAX_PHYSICS_STEP$
   double DEFAULT_BALL_RADIUS$
   double DEFAULT_BALL_SPEED$
   double DEFAULT_PADDLE_SPEED$
   double DEFAULT_PADDLE_WIDTH$
   double DEFAULT_PADDLE_HEIGHT$
   int MAX_TRAIL_EFFECTS$
   int MAX_PARTICLES$
   int MAX_BULLETS$
   int MAX_FLOATING_TEXTS$
   int MAX_LEVEL$
}
class GameHudPanel {
  + GameHudPanel(int, boolean) 
  - createInfoRow(String, String, Color) HBox
  + updateStats(int, int, int, int) void
  + updateFps(boolean, double, double) void
   String formattedTime
}
class GameLogger {
  - GameLogger() 
  + getLogger(String) Logger
  + logThreadInfo(Logger, String) void
  + setThreadContext(String, Object) void
  + logCollectionState(Logger, String, Collection~?~) void
  + clearThreadContext() void
  + getLogger(Class~?~) Logger
   Map~String, String~ threadContext
}
class GameManager {
  - GameManager(double, double, int) 
  + onBrickDestroyed(Brick) void
  + togglePause() void
  - resetBall(Paddle) void
  + pause() void
  + resume() void
  + restoreGameState(GameState) void
  + extractCurrentGameState() GameState
  - applyPowerUpEffect(PowerUp) void
  - stepSimulation(double) void
  + loadLevel(int) void
  + getInstance(double, double, int) GameManager
  + addExplosion(double, double, double, double, double) void
  + update(double) void
  + startGame() void
  + cleanup() void
   List~Ball~ balls
   GameState currentState
   PlayerManager playerManager
   VisualEffectsManager visualEffectsManager
   List~Explosion~ explosions
   List~Brick~ bricks
   int score
   List~FloatingText~ floatingTexts
   List~Particle~ particles
   List~PowerUp~ powerUps
   List~Bullet~ bullets
   int levelNumber
   Player player
   List~LineEffect~ lineEffects
   double elapsedTimeSeconds
   List~TrailEffect~ trailEffects
}
class GameObject {
  + GameObject(double, double, double, double) 
  + update(double) void
  + intersects(GameObject) boolean
  + render(GraphicsContext) void
   double width
   double height
   double x
   double y
   double centerY
   double centerX
   boolean active
}
class GameOver {
  + GameOver() 
  - onBackHomeClick() void
  - onNewGameClick() void
  + init(int, int, String) void
  - saveHighScore(int) void
}
class GamePauseOverlay {
  + GamePauseOverlay(double, double, Runnable, Runnable, Runnable, Runnable) 
  - createStyledButton(String, Runnable) Button
}
class GameSave {
  + GameSave(int, int, String, int, int, int, int, byte[], byte[], LocalDateTime) 
   LocalDateTime createdAt
   byte[] thumbnailData
   String saveName
   int lives
   int levelNumber
   int userId
   int id
   int score
   byte[] compressedGameState
   int elapsedTimeSeconds
}
class GameSaveListCell {
  + GameSaveListCell() 
  - loadThumbnail(GameSave) void
  # updateItem(GameSave, boolean) void
  + clearCache() void
  + removeCachedThumbnail(Long) void
}
class GameSaveManager {
<<Interface>>
  + loadGame(int) GameSave
  + getAllSaves(int) ObservableList~GameSave~
  + saveCurrentGame(int, String, WritableImage) GameSave
  + deleteAllSaves(int) void
  + getSaveCount(int) int
  + saveCurrentGameWithAutoName(int, WritableImage) GameSave
  + isValidSaveName(String) boolean
  + canSaveCurrentGame() boolean
  + deleteSave(int) void
  + getSaveById(int) Optional~GameSave~
}
class GameSaveManagerImpl {
  + GameSaveManagerImpl(GameManager) 
  + saveCurrentGameWithAutoName(int, WritableImage) GameSave
  + saveCurrentGameWithAutoNameAsync(int, WritableImage) CompletableFuture~GameSave~
  - deleteOldestSave(int) void
  + saveCurrentGameAsync(int, String, WritableImage) CompletableFuture~GameSave~
  + deleteSaveAsync(int) CompletableFuture~Void~
  + getSaveCount(int) int
  + deleteAllSaves(int) void
  + loadGameAsync(int) CompletableFuture~GameSave~
  + loadGame(int) GameSave
  + getSaveById(int) Optional~GameSave~
  + getAllSaves(int) ObservableList~GameSave~
  + deleteSave(int) void
  + getAllSavesAsync(int) CompletableFuture~ObservableList~GameSave~~
  + saveCurrentGame(int, String, WritableImage) GameSave
  + isValidSaveName(String) boolean
  + canSaveCurrentGame() boolean
}
class GameSaveRepository {
<<Interface>>
  + findByUserId(int) List~GameSave~
  + deleteById(int) boolean
  + countByUserId(int) int
  + findById(int) Optional~GameSave~
  + create(int, String, int, int, int, int, byte[], byte[]) GameSave
}
class GameSaveRepositoryImpl {
  + GameSaveRepositoryImpl(DatabaseManager) 
  - mapResultSetToGameSave(ResultSet) GameSave
  + deleteById(int) boolean
  + create(int, String, int, int, int, int, byte[], byte[]) GameSave
  + findById(int) Optional~GameSave~
  + findByUserId(int) List~GameSave~
  + countByUserId(int) int
}
class GameScene {
  - GameScene(Stage, double, double, int) 
  + getInstance(Stage, double, double, int) GameScene
  + onEnter() void
  + onExit() void
  + start() void
  - openSaveLoadScene() void
  + onSaveLoaded(int) void
  + cleanup() void
  - decreaseLogLevel() void
  - increaseLogLevel() void
  + hidePauseOverlay() void
  - update(double) void
  - syncHudFromManager() void
  - render() void
  + captureCanvasSnapshot() WritableImage
   Scene scene
   boolean debugMode
   Canvas canvas
}
class GameState {
  + GameState() 
  + GameState(int, int, int, int, PaddleState, List~BallState~, List~BrickState~, List~PowerUpState~) 
   PaddleState paddleState
   int lives
   int levelNumber
   List~PowerUpState~ activePowerUps
   int score
   List~BallState~ ballStates
   List~BrickState~ brickStates
   int elapsedTimeSeconds
}
class GameStateMapper {
  - GameStateMapper() 
  + toGameState(int, double, Player, List~Ball~, List~Brick~, List~PowerUp~) GameState
  + applyGameState(GameState, double, double, Player, List~Ball~, List~Brick~, List~PowerUp~) double
  - createBrickFromState(BrickState, double) Brick
  - createPowerUpFromState(PowerUpState) PowerUp
}
class GameStateSerializer {
<<Interface>>
  + isValidJson(String) boolean
  + isValidGameState(GameState) boolean
  + extractMetadata(String) GameStateMetadata
  + toJson(GameState) String
  + fromJson(String) GameState
}
class GameStateSerializerImpl {
  + GameStateSerializerImpl() 
  + fromJson(String) GameState
  + isValidJson(String) boolean
  + isValidGameState(GameState) boolean
  + toJson(GameState) String
  + extractMetadata(String) GameStateMetadata
}
class GunPaddlePowerUp {
  + GunPaddlePowerUp(double, double) 
  + removeEffect(Paddle) void
  + applyEffect(Paddle) void
}
class ImageLoader {
  - ImageLoader() 
  + preloadGameImagesAsync() CompletableFuture~Void~
  + clearCache() void
  + preloadImagesAsync(String[]) CompletableFuture~Void~
  + getCachedImage(String) Image
  + isCached(String) boolean
  + loadImageAsync(String) CompletableFuture~Image~
  + loadImage(String) Image
   int cacheSize
}
class InputHandler {
  + InputHandler(GameManager) 
  - launchAllBalls() void
  + handleKeyPress(KeyCode) boolean
  + handleContinuousInput(Set~KeyCode~, double) void
   GameManager gameManager
}
class InventoryManager {
  + InventoryManager() 
  + hasItem(int, String) boolean
  + removeItem(int, String) boolean
  + getItemQuantity(int, String) int
  + addItem(int, String, int) boolean
  + getUserInventory(int) List~InventoryItem~
}
class LeaderboardController {
  + LeaderboardController() 
  + refreshData() void
  + initialize() void
  - onBackClick() void
  - setupCellFactory() void
}
class LevelManager {
  + LevelManager() 
  + loadLevel(int) List~Brick~
  + nextLevel() void
   int currentLevel
}
class LineEffect {
  + LineEffect(double, double, double, double, double) 
  + render(GraphicsContext) void
  + update(double) void
  - generateLightning() void
}
class LoggingConfig {
  + LoggingConfig() 
  + applyEnvironmentVariables() void
   String logPattern
   boolean fileEnabled
   boolean consoleEnabled
   Level logLevel
   String logFilePath
}
class LoggingManager {
  - LoggingManager() 
  + awaitTermination(long, TimeUnit) void
  + shutdown() void
  + initialize(LoggingConfig) void
   LoggingConfig config
   LoggingManager instance
   Level logLevel
   boolean initialized
}
class MainMenuView {
  + MainMenuView() 
  - createModeSelectPopup() VBox
  + onLeaderBoardClick(MouseEvent) void
  + onShopClick(MouseEvent) void
  - styleModeSelectButton(Button) void
  - applyIntroEffect() void
  + onStartGameClick(MouseEvent) void
  + onQuitClick(MouseEvent) void
  + onProfileClick(MouseEvent) void
  + initialize() void
  + onSettingClick(MouseEvent) void
  + closeModePopup() void
}
class Map {
  + Map() 
  - onPlayGameButtonClick(MouseEvent) void
  - updatePreview() void
  + initialize() void
  + onPreMapButtonClick(MouseEvent) void
  - onBackToMainMenuClick(MouseEvent) void
  + onNextMapButtonClick(MouseEvent) void
   Stage stage
   int unlockedLevel
}
class MemoryMonitor {
  - MemoryMonitor() 
  + trackGameManager(GameManager) void
  - updateStats(String, int) void
  - reportMemoryUsage(long, long, long, double) void
  - suggestGC() void
  - trackCollections() void
  - reportTopCollections() void
  + shutdown() void
  - checkAndReport() void
   MemoryMonitor instance
   boolean monitoringEnabled
   boolean enabled
}
class MovableObject {
  + MovableObject(double, double, double, double, double) 
  + reverseY() void
  + move(double) void
  + reverseX() void
   double velocityX
   double speed
   double velocityY
}
class MovingBrick {
  + MovingBrick(double, double, double, double, double, double, int, int, String) 
  + update(double) void
}
class MultiBallPowerUp {
  + MultiBallPowerUp(double, double) 
  + applyEffect(Paddle) void
  + removeEffect(Paddle) void
}
class NormalBrick {
  + NormalBrick(double, double, double, double, int, int, String) 
}
class ObjectPool~T~ {
  + ObjectPool(Supplier~T~, Consumer~T~, int) 
  + acquire() T
  + clear() void
  + prewarm(int) void
  + release(T) void
   int availableCount
   int totalCount
}
class Orientation {
<<enumeration>>
  - Orientation(int) 
  + fromPlayerNumber(int) Orientation
  + values() Orientation[]
  + valueOf(String) Orientation
   int directionMultiplier
}
class Paddle {
  + Paddle(double, double, double, double, double, double, double) 
  + Paddle(double, double, double, double) 
  + stop() void
  + expand(double) void
  + getSkin(String) Image
  + update(double) void
  + render(GraphicsContext) void
  - loadSkins() void
  - constrainToBounds() void
  + resetSize() void
  + moveLeft(double) void
  + moveRight(double) void
  + equipSkin(String) void
  + triggerHitFlash(double, double, double) void
   String currentSkin
   double rightGunX
   Color color
   double gunY
   boolean gunMode
   double leftGunX
   Image paddleImage
   long gunExpiry
}
class PaddleState {
  + PaddleState(double, double, double, double, double, String, String, long) 
  + x() double
  + width() double
  + height() double
  + velocityX() double
  + equippedSkin() String
  + activePowerUp() String
  + y() double
  + powerUpExpiryNano() long
}
class Particle {
  + Particle(double, double, Color) 
  + update(double) void
  + reset(double, double, Color) void
  + render(GraphicsContext) void
}
class PasswordHasher {
  + PasswordHasher() 
  + hashPassword(String) String
  + verifyPassword(String, String) boolean
  - generateSalt() byte[]
  - hashWithSalt(String, byte[]) String
}
class Player {
  + Player(String, int, Paddle) 
  + update(double) void
   int playerNumber
   Orientation orientation
   Paddle paddle
   Ball ball
   PlayerState state
   PlayerProfile profile
}
class PlayerManager {
  - PlayerManager() 
  + update(double) void
  + handleInput(KeyCode, boolean, double) void
  + addPlayer(int, Player) void
  + getPlayer(int) Player
   PlayerManager instance
}
class PlayerProfile {
  + PlayerProfile(String) 
  + hasItem(String) boolean
  + addItem(String, int) void
   String currentSkin
   String playerId
   PlayerProfile currentPlayer
   String equippedPaddleSkin
}
class PlayerProfile {
  + PlayerProfile(int, int, int, String, int, int) 
  + incrementGamesPlayed() void
  + addToTotalScore(int) void
   int totalScore
   String currentSkin
   int highScore
   int userId
   int money
   int gamesPlayed
}
class PlayerProfileManager {
  + PlayerProfileManager() 
  + addToTotalScore(int, int) void
  + incrementGamesPlayed(int) void
  + updateMoney(int, int) void
  + updateHighScore(int, int) void
  + getProfile(int) ProfileData?
  + updateSkin(int, String) void
  - executeUpdate(String, Object[]) void
   List~LeaderboardEntry~ leaderboardData
}
class PlayerProfileRepository {
<<Interface>>
  + update(PlayerProfile) void
  + create(int) PlayerProfile
  + findByUserId(int) Optional~PlayerProfile~
  + getLeaderboard(int) List~PlayerProfile~
}
class PlayerProfileRepositoryImpl {
  + PlayerProfileRepositoryImpl(DatabaseManager) 
  + create(int) PlayerProfile
  + findByUserId(int) Optional~PlayerProfile~
  + getLeaderboard(int) List~PlayerProfile~
  - mapResultSetToProfile(ResultSet) PlayerProfile
  + update(PlayerProfile) void
}
class PlayerState {
  + PlayerState() 
  + addLife() void
  + loseLife() void
  + reset() void
  + addScore(int) void
  + nextLevel() void
   int level
   int score
   int lives
   boolean gameOver
}
class PowerUp {
  + PowerUp(double, double, double) 
  + render(GraphicsContext) void
  + update(double) void
  + removeEffect(Paddle) void
  + applyEffect(Paddle) void
  + checkPaddleCollision(Paddle) boolean
   boolean collected
}
class PowerUpService {
<<Interface>>
  + applyPickup(int, PowerUp) void
  + updateAll(double) void
  + spawn(double, double, int) void
  + clear() void
}
class PowerUpServiceImpl {
  + PowerUpServiceImpl(Player, Player) 
  + spawn(double, double, int) void
  - createRandomPowerUp(double, double) PowerUp
  + applyPickup(int, PowerUp) void
  + clear() void
  + updateAll(double) void
  - isOutOfBounds(PowerUp) boolean
}
class PowerUpState {
  + PowerUpState(String, double, double, double, boolean) 
  + x() double
  + velocityY() double
  + type() String
  + active() boolean
  + y() double
}
class ProfileScreen {
  + ProfileScreen() 
  + initialize() void
  ~ onLogOutClick(MouseEvent) void
  ~ onBackClick(MouseEvent) void
  + refreshProfile() void
}
class RepositoryFactory {
  - RepositoryFactory() 
  + shutdown() void
   PlayerProfileRepository playerProfileRepository
   UserRepository userRepository
   DatabaseManager databaseManager
   RepositoryFactory instance
   GameSaveRepository gameSaveRepository
   PasswordHasher passwordHasher
}
class RespawnService {
<<Interface>>
  + respawnBall(int) void
   double launchSpeed
}
class RespawnServiceImpl {
  + RespawnServiceImpl(Player, Player, double) 
  + respawnBall(int) void
   double launchSpeed
}
class RowClearPowerUp {
  + RowClearPowerUp(double, double) 
  + removeEffect(Paddle) void
  + applyEffect(Paddle) void
}
class SaveLoadScene {
  + SaveLoadScene() 
  - showError(String) void
  - setupKeyboardShortcuts() void
  - refreshSaveList() void
  - showSuccess(String) void
  + init(GameSaveManager, GameManager, Stage, int, Runnable, StackPane, GameScene) void
  + onLoadGame(ActionEvent) void
  + onDeleteGame(ActionEvent) void
  + onBack(ActionEvent) void
  + onSaveNewGame(ActionEvent) void
}
class SceneLifecycle {
<<Interface>>
  + onEnter() void
  + onExit() void
}
class SceneManager {
  + SceneManager() 
  + switchTo(String) void
  + showCustomRoot(String, Parent, SceneLifecycle) void
  + loadScene(String, String) void
  + setKeyHandlers(EventHandler~KeyEvent~, EventHandler~KeyEvent~) void
  + getController(String) Object
  - refreshSceneIfNeeded(String) void
  + showWinLevel(int, int, String) void
  + showGameOver(int, int, String) void
  + cleanup() void
   Stage stage
   Scene mainScene
   String activeSceneName
   GameScene activeGameScene
}
class SessionManager {
  + SessionManager() 
  + login(int, String, String, boolean) void
  + login(int, String, String) void
  + restoreSession() boolean
  + savePlayer(PlayerProfile) void
  + logout() void
   String equippedSkin
   PlayerProfile activeProfile
   String equippedBallSkin
   boolean loggedIn
   String equippedPaddleSkin
   User? currentUser
}
class SettingView {
  + SettingView() 
  - savePreferences() void
  - onOKClick() void
  - setupVolumeListener() void
  + initialize() void
  - onCancelClick() void
  - loadUserPreferences() void
}
class ShopBall {
  + ShopBall() 
  + onEquip2Click(MouseEvent) void
  + onBackClick(MouseEvent) void
  + onEquip1Click(MouseEvent) void
  + onBuy1Click(MouseEvent) void
  + onEquip3Click(MouseEvent) void
  + onBuy2Click(MouseEvent) void
  - buySkin(String) void
  + onBuy3Click(MouseEvent) void
  + refreshMoney() void
  - updateShopUI() void
  + setPlayer(PlayerProfile, Ball) void
  + initialize() void
  - equipSkin(String) void
}
class ShopPaddle {
  + ShopPaddle() 
  + onEquip3Click(MouseEvent) void
  - equipSkin(String) void
  + initialize() void
  + onEquip2Click(MouseEvent) void
  + onBackHomeClick(MouseEvent) void
  - updateShopUI() void
  + refreshMoney() void
  + onBuy1Click(MouseEvent) void
  + onEquip1Click(MouseEvent) void
  + onBuy3Click(MouseEvent) void
  + onBuy2Click(MouseEvent) void
  - buySkin(String) void
   Paddle player
}
class ShopView {
  + ShopView() 
  - updateMoneyLabel() void
  - showMessage(String) void
  + initialize() void
  + onBallShopClick(MouseEvent) void
  + onPaddleShopClick(MouseEvent) void
  + onDepositClick(MouseEvent) void
  + onBackClick(MouseEvent) void
  + refreshMoney() void
}
class SignIn {
  + SignIn() 
  + onSignUpLinkClick(MouseEvent) void
  + onSignInClick() void
  + onCancelClick(MouseEvent) void
}
class SignUpView {
  + SignUpView() 
  + onCancelClick(MouseEvent) void
  + onSignInLinkClick(MouseEvent) void
  + onSignUpClick() void
}
class SoundManager {
  - SoundManager() 
  + preloadSoundsAsync(String[]) CompletableFuture~Void~
  + stopBackground() void
  + playBackground(String, boolean) void
  + playSound(String) void
  + preloadGameSoundsAsync() CompletableFuture~Void~
   SoundManager instance
   double volume
}
class StrongBrick {
  + StrongBrick(double, double, double, double, int, int, String) 
  + hit() void
  + destroy() void
  + render(GraphicsContext) void
  + instantDestroy() void
  - loadImage(String) Image?
   boolean destroyed
}
class ThreadContext {
  + ThreadContext() 
  + unregister() void
  + clear() void
  + getMetadata(long) ThreadMetadata
  + register(String) void
  + get(String) Object
  + set(String, Object) void
  + remove(String) void
   ThreadMetadata currentMetadata
   Map~Long, ThreadMetadata~ allMetadata
   Map~String, Object~ all
}
class ThreadManager {
  - ThreadManager() 
  + shutdown() void
  + executeBackground(Callable~T~, String) Future~T~
  + executeOnGameLoop(Runnable, String) Future~?~
  + scheduleWithFixedDelay(Runnable, long, long, TimeUnit, String) ScheduledFuture~?~
  + executeBackground(Runnable, String) Future~?~
  + schedule(Runnable, long, TimeUnit, String) ScheduledFuture~?~
   ThreadManager instance
   boolean terminated
}
class ThumbnailCapture {
<<Interface>>
  + estimateThumbnailSizeBytes(int, int) int
  + loadThumbnailFromBytes(byte[]) Image
  + calculateThumbnailDimensions(double, double) ThumbnailDimensions
  + isValidPngBytes(byte[]) boolean
  + captureThumbnailPNG(WritableImage) byte[]
  + isValidCanvas(WritableImage) boolean
  + captureThumbnailPNG(WritableImage, int, int) byte[]
}
class ThumbnailCaptureImpl {
  + ThumbnailCaptureImpl() 
  + calculateThumbnailDimensions(double, double) ThumbnailDimensions
  - scaleImage(BufferedImage, int, int) BufferedImage
  + captureThumbnailPNG(WritableImage) byte[]
  + isValidCanvas(WritableImage) boolean
  + isValidPngBytes(byte[]) boolean
  + captureThumbnailPNG(WritableImage, int, int) byte[]
  + estimateThumbnailSizeBytes(int, int) int
  + loadThumbnailFromBytes(byte[]) Image
}
class ToastNotification {
  + ToastNotification() 
  + showToast(String, Region) void
  - getStyleForType(ToastType) String
  + showToast(String, Region, ToastType) void
}
class TrailEffect {
  + TrailEffect(double, double, double, double, Color) 
  + reset(double, double, double, double, Color) void
  + update(double) void
  + render(GraphicsContext) void
   boolean active
}
class TwoPlayerGameOverScreen {
  + TwoPlayerGameOverScreen(Stage, int, EndReason, int, int) 
  - startRematch() void
  + show() void
  - returnToMenu() void
   String resultMessage
}
class TwoPlayerGameScreen {
  + TwoPlayerGameScreen(Stage) 
  - resumeGame() void
  - initializeGame() void
  - showPauseMenu() void
  + cleanup() void
  - handleBallBrickCollision(Ball, Brick) void
  - update(double) void
  - createMenuButton(String) Button
  - startGameLoop() void
  - backToMenu() void
  - showGameOver() void
  - loadBackgroundImage() void
  - setupInput(Scene) void
  - checkBrickCollisions() void
  + show() void
  - hidePauseMenu() void
  - restartGame() void
  - render() void
}
class TwoPlayerInputHandler {
  + TwoPlayerInputHandler(Player, Player, TwoPlayerMatchManager) 
  - launchAllBalls() void
  - togglePause() void
  + handleKeyPress(KeyCode) boolean
  + handleContinuousInput(Set~KeyCode~, double) void
   TwoPlayerMatchManager matchManager
   Runnable onResumeCallback
   Runnable onPauseCallback
}
class TwoPlayerMatchManager {
<<Interface>>
  + endMatch(EndReason) void
  + resume() void
  + update(double) void
  + startMatch() void
  + handleLifeLoss(int, LifeLossCause) void
  + applyBrickHit(int, int) void
  + pause() void
   MatchState state
}
class TwoPlayerMatchManagerImpl {
  + TwoPlayerMatchManagerImpl(Player, Player, CollisionService, RespawnService, PowerUpService, List~Brick~) 
  + TwoPlayerMatchManagerImpl(Player, Player, CollisionService, RespawnService, PowerUpService) 
  - checkWinConditions() void
  - areAllBricksDestroyed() boolean
  + handleLifeLoss(int, LifeLossCause) void
  + endMatch(EndReason) void
  + resume() void
  + update(double) void
  + startMatch() void
  + applyBrickHit(int, int) void
  + pause() void
   EndReason endReason
   MatchState state
   int winningPlayer
   Player player2
   Player player1
}
class TwoPlayerStatsPanel {
  + TwoPlayerStatsPanel(Player, Player) 
  + update() void
  - createPlayerSection(int) VBox
  - setupUI() void
}
class UnbreakableBrick {
  + UnbreakableBrick(double, double, double, double, int, int, String) 
  + hit() void
}
class User {
  + User(int, String, String, LocalDateTime, LocalDateTime) 
   String passwordHash
   LocalDateTime createdAt
   String username
   LocalDateTime lastLogin
   int id
}
class UserManager {
  + UserManager() 
  + getProfile(int) PlayerProfile
  + login(String, String) User?
  + register(String, String) User?
  + usernameExists(String) boolean
}
class UserPreferences {
  + UserPreferences() 
  + UserPreferences(int) 
  + toString() String
   int musicVolume
   int userId
   double musicVolumeAsDouble
}
class UserPreferencesManager {
  + UserPreferencesManager() 
  + savePreferences(UserPreferences) void
  + getPreferences(int) Optional~UserPreferences~
  + updateMusicVolume(int, int) void
}
class UserRepository {
<<Interface>>
  + findById(int) Optional~User~
  + findByUsername(String) Optional~User~
  + existsByUsername(String) boolean
  + create(String, String) User
  + updateLastLogin(int) void
}
class UserRepositoryImpl {
  + UserRepositoryImpl(DatabaseManager) 
  + create(String, String) User
  + existsByUsername(String) boolean
  + updateLastLogin(int) void
  - mapResultSetToUser(ResultSet) User
  + findById(int) Optional~User~
  + findByUsername(String) Optional~User~
}
class UserSessionStorage {
  + UserSessionStorage() 
  + loadSession() SessionData?
  + clearSession() boolean
  + hasSession() boolean
  + saveSession(int, String, String) boolean
}
class ViewportScaler {
  - ViewportScaler() 
  + wrap(Parent, double, double) StackPane
}
class VisualEffectsManager {
  + VisualEffectsManager() 
  + spawnBrickParticles(double, double, Color, int) void
  + spawnFloatingText(String, double, double, double, double, Color) void
  + spawnExplosion(double, double, double, double, double) void
  + spawnLineEffect(double, double, double, double, double) void
  + spawnBallTrails(List~Ball~, double) void
  + update(double) void
  + render(GraphicsContext) void
  + clearAndReleaseAll() void
  + cleanup() void
   List~Explosion~ explosions
   List~LineEffect~ lineEffects
   List~Particle~ particles
   List~TrailEffect~ trailEffects
   List~FloatingText~ floatingTexts
   ObjectPool~Particle~ particlePool
   ObjectPool~TrailEffect~ trailPool
   ObjectPool~FloatingText~ floatingTextPool
}
class WinLevel {
  + WinLevel() 
  - onNextLevelClick() void
  + init(int, int, String) void
  - onPreLevelClick() void
  - onHomeClick() void
  - onReplayClick() void
  - saveHighScore(int) void
}

Ball  -->  MovableObject 
BaseBrick  ..>  Brick 
BaseBrick  -->  GameObject 
Bullet  -->  GameObject 
CollisionServiceImpl  ..>  CollisionService 
DuplicateEntityException  -->  DatabaseException 
EntityNotFoundException  -->  DatabaseException 
ExpandPaddlePowerUp  -->  PowerUp 
Explosion  -->  GameObject 
ExplosiveBallPowerUp  -->  PowerUp 
FloatingText  -->  GameObject 
GameSaveManagerImpl  ..>  GameSaveManager 
GameSaveRepositoryImpl  ..>  GameSaveRepository 
GameStateSerializerImpl  ..>  GameStateSerializer 
GunPaddlePowerUp  -->  PowerUp 
InputHandler  -->  BaseInputHandler 
LineEffect  -->  GameObject 
MovableObject  -->  GameObject 
MovingBrick  -->  BaseBrick 
MultiBallPowerUp  -->  PowerUp 
NormalBrick  -->  BaseBrick 
Paddle  -->  MovableObject 
Particle  -->  GameObject 
PlayerProfileRepositoryImpl  ..>  PlayerProfileRepository 
PowerUp  -->  MovableObject 
PowerUpServiceImpl  ..>  PowerUpService 
RespawnServiceImpl  ..>  RespawnService 
RowClearPowerUp  -->  PowerUp 
StrongBrick  -->  BaseBrick 
ThumbnailCaptureImpl  ..>  ThumbnailCapture 
TrailEffect  -->  GameObject 
TwoPlayerInputHandler  -->  BaseInputHandler 
TwoPlayerMatchManagerImpl  ..>  TwoPlayerMatchManager 
UnbreakableBrick  -->  BaseBrick 
UserRepositoryImpl  ..>  UserRepository 
GameScene  ..>  SceneLifecycle 
MainMenuView  ..>  SceneLifecycle 
SettingView  ..>  SceneLifecycle 
ShopBall  ..>  SceneLifecycle 
ShopPaddle  ..>  SceneLifecycle 
TwoPlayerGameScreen  ..>  SceneLifecycle 
GameScene  -->  GameHudPanel 
GameScene  -->  GamePauseOverlay 
GameScene  ..>  ViewportScaler 
GameScene  ..>  GameConstants 
SceneManager  ..>  ViewportScaler 
SceneManager  -->  SceneLifecycle 
TwoPlayerGameScreen  ..>  ViewportScaler 
ViewportScaler  ..>  GameConstants 
GameManager  -->  VisualEffectsManager 
GameManager  ..>  GameStateMapper 
GameManager  ..>  GameConstants 
VisualEffectsManager  ..>  GameConstants 
