package com.protonmail.landrevillejf.ide.plugin.ui.listener;

/**
 * Listener for build events from the BuildManager.
 * <p>
 * Provides callbacks for build lifecycle events including start, progress,
 * completion, failure, and cancellation.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public interface BuildListener {
    /**
     * Called when a build task starts.
     * @param taskName the name of the task
     */
    void onBuildStarted(String taskName);

    /**
     * Called when a build task makes progress.
     * @param message the progress message
     */
    void onBuildProgress(String message);

    /**
     * Called when a build task completes successfully.
     * @param taskName the name of the task
     * @param duration the duration in milliseconds
     */
    void onBuildCompleted(String taskName, long duration);

    /**
     * Called when a build task fails.
     * @param taskName the name of the task
     * @param errorMessage the error message
     */
    void onBuildFailed(String taskName, String errorMessage);

    /**
     * Called when a build task is cancelled.
     * @param taskName the name of the task
     */
    void onBuildCancelled(String taskName);
}