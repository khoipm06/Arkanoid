package com.arkanoid.ui.view;

/**
 * Lifecycle hooks for views and dynamic screens managed by {@link SceneManager}.
 */
public interface SceneLifecycle {

    /**
     * Called right after this view's root is mounted into the persistent main {@code Scene}.
     */
    default void onEnter() {
    }

    /**
     * Called right before switching away from this view or during application cleanup.
     */
    default void onExit() {
    }
}
