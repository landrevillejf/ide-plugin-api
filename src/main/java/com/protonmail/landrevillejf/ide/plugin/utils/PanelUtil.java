package com.protonmail.landrevillejf.ide.plugin.utils;

import com.protonmail.landrevillejf.swingide.core.bus.EventBus;
import com.protonmail.landrevillejf.swingide.core.bus.events.PanelAddRequest;
import com.protonmail.landrevillejf.swingide.core.bus.events.PanelAddResponse;
import com.protonmail.landrevillejf.swingide.core.bus.events.PanelRemoveRequest;
import com.protonmail.landrevillejf.swingide.core.layout.IdePanelRegion;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.concurrent.*;

/**
 * Utilitaire pour que les plugins ajoutent facilement des panneaux
 */
@Slf4j
public class PanelUtil {

    private final EventBus eventBus;
    private final String pluginId;
    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public PanelUtil(final EventBus eventBus, final String pluginId) {
        this.eventBus = eventBus;
        this.pluginId = pluginId;
    }

    /**
     * Ajoute un panneau de façon asynchrone (String location, pour compatibilité)
     */
    public CompletableFuture<Boolean> addPanel(final String title, final Icon icon, final JPanel panel, final String location) {
        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        final String generatedPanelId = generatePanelId();

        final PanelAddRequest request = new PanelAddRequest(pluginId, title, icon, panel, location);

        subscribeToResponse(future, generatedPanelId, request);
        publishRequest(request);
        setupTimeout(future);

        return future;
    }

    /**
     * Ajoute un panneau de façon asynchrone en utilisant une région typée.
     */
    public CompletableFuture<Boolean> addPanel(final String title, final Icon icon, final JPanel panel, final IdePanelRegion region) {
        final String location = toLocation(region);
        return addPanel(title, icon, panel, location);
    }

    /**
     * Ajoute un panneau de façon synchrone (String location, pour compatibilité)
     */
    public boolean addPanelSync(final String title, final Icon icon, final JPanel panel, final String location) {
        final CompletableFuture<Boolean> future = addPanel(title, icon, panel, location);
        return waitForCompletion(future);
    }

    /**
     * Ajoute un panneau de façon synchrone en utilisant une région typée.
     */
    public boolean addPanelSync(final String title, final Icon icon, final JPanel panel, final IdePanelRegion region) {
        final String location = toLocation(region);
        return addPanelSync(title, icon, panel, location);
    }

    /**
     * Supprime un panneau
     */
    public void removePanel(final String panelId) {
        eventBus.publish(new PanelRemoveRequest(pluginId, panelId));
    }

    /**
     * Supprime tous les panneaux de ce plugin (à implémenter ultérieurement si besoin)
     */
    public void removeAllPanels() {
        // Cette méthode nécessite que le PanelManager ait une méthode pour ça
        // ou on peut garder une liste locale des panelIds
        if (log.isDebugEnabled()) {
            log.debug("removeAllPanels called for plugin: {}", pluginId);
        }
    }

    /**
     * Shutdown executor service when done
     */
    public void shutdown() {
        scheduledExecutor.shutdown();
        try {
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduledExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public String generatePanelId() {
        return pluginId + "-" + System.currentTimeMillis();
    }

    private void subscribeToResponse(final CompletableFuture<Boolean> future, final String panelId, final PanelAddRequest request) {
        eventBus.subscribe(PanelAddResponse.class, response -> {
            if (response.getPluginId().equals(pluginId) &&
                    response.getPanelId().equals(request.getPanelId())) {
                final boolean isSuccess = response.isSuccess();
                future.complete(isSuccess);
                if (!isSuccess && log.isErrorEnabled()) {
                    log.error("Failed to add panel: {}", response.getMessage());
                }
            }
        });
    }

    private void publishRequest(final PanelAddRequest request) {
        eventBus.publish(request);
    }

    private void setupTimeout(final CompletableFuture<Boolean> future) {
        scheduledExecutor.schedule(() -> {
            if (!future.isDone()) {
                future.complete(false);
            }
        }, 5, TimeUnit.SECONDS);
    }

    private boolean waitForCompletion(final CompletableFuture<Boolean> future) {
        boolean result = false;
        try {
            result = future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            if (log.isErrorEnabled()) {
                log.error("Interrupted while waiting for panel addition", e);
            }
            Thread.currentThread().interrupt();
        } catch (TimeoutException e) {
            if (log.isErrorEnabled()) {
                log.error("Timeout while waiting for panel addition", e);
            }
        } catch (ExecutionException e) {
            if (log.isErrorEnabled()) {
                log.error("Execution error while waiting for panel addition", e);
            }
        }
        return result;
    }

    private String toLocation(final IdePanelRegion region) {
        if (region == null) {
            return "center";
        }
        return switch (region) {
            case LEFT -> "left";
            case RIGHT -> "right";
            case BOTTOM -> "bottom";
            case CENTER -> "center";
            default -> "center";
        };
    }
}