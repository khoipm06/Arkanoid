/**
 * Core domain model, canonical simulation constants, and deterministic collision primitives.
 *
 * <p>This package defines {@link com.arkanoid.core.GameConstants} alongside the entity hierarchy
 * ({@code com.arkanoid.core.entities}) and collision resolution
 * ({@code com.arkanoid.core.physics}). Core entities and physics routines are strictly decoupled
 * from background persistence and are driven on the JavaFX Application Thread using fixed-cap
 * sub-stepping ({@link com.arkanoid.core.GameConstants#MAX_FRAME_DELTA} and
 * {@link com.arkanoid.core.GameConstants#MAX_PHYSICS_STEP} at 120 Hz) to preserve deterministic
 * trajectories across variable frame rates.
 *
 * <p><strong>Thread-safety boundary:</strong> Entity state is non-synchronized and must only be
 * mutated by the active simulation owner on the JavaFX Application Thread, or serialized via
 * immutable state snapshots before crossing into background I/O.
 */
package com.arkanoid.core;
