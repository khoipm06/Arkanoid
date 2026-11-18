/**
 * Runtime simulation coordinators, input handling, audio, logging, persistence mapping, and
 * concurrency infrastructure.
 *
 * <p>{@link com.arkanoid.systems.GameManager} orchestrates single-player gameplay using a
 * deterministic sub-stepping loop bounded by {@link com.arkanoid.core.GameConstants#MAX_PHYSICS_STEP}
 * (120 Hz), while {@link com.arkanoid.systems.VisualEffectsManager} and bounded
 * {@link com.arkanoid.utils.ObjectPool} instances reuse bullets, particles, trails, and floating
 * text without per-frame heap churn.
 *
 * <p><strong>Thread-safety boundaries:</strong>
 * <ul>
 *   <li>Simulation updates, entity mutation, and {@code Canvas} rendering run exclusively on the
 *       JavaFX Application Thread.</li>
 *   <li>Asynchronous asset preloading, LZ4 save-state compression, and database operations are
 *       dispatched through {@link com.arkanoid.systems.threading.ThreadManager}'s daemon worker
 *       pools and never mutate live scene graphs directly.</li>
 *   <li>Session teardown must invoke {@code cleanup()} to release pooled effects back to their
 *       pools before discarding manager references.</li>
 * </ul>
 */
package com.arkanoid.systems;
