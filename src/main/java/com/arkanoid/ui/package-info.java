/**
 * JavaFX presentation layer, persistent viewport management, HUD components, and screen lifecycle.
 *
 * <p>All views and gameplay canvases share a single persistent {@link javafx.scene.Scene} hosted
 * by {@link com.arkanoid.ui.view.SceneManager} and wrapped by
 * {@link com.arkanoid.ui.view.ViewportScaler} to maintain consistent scaling and input hitboxes
 * without recreating OS windows or leaking scene-level event handlers.
 *
 * <p><strong>{@link com.arkanoid.ui.view.SceneLifecycle} invariants:</strong>
 * <ul>
 *   <li>Every scene transition clears active keyboard handlers and invokes {@code onExit()} on the
 *       outgoing controller before mounting the incoming root and calling {@code onEnter()}.</li>
 *   <li>Gameplay views ({@link com.arkanoid.ui.GameScene} and
 *       {@link com.arkanoid.ui.view.TwoPlayerGameScreen}) must stop their
 *       {@link javafx.animation.AnimationTimer}, clear input state, untrack memory monitors, and
 *       release pooled simulation resources idempotently inside {@code onExit()} / {@code cleanup()}.</li>
 *   <li>All scene-graph mutations and {@link javafx.scene.canvas.GraphicsContext} draws execute
 *       strictly on the JavaFX Application Thread.</li>
 * </ul>
 */
package com.arkanoid.ui;
