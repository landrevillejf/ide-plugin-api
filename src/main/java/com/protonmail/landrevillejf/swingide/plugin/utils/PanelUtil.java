package com.protonmail.landrevillejf.swingide.plugin.utils;

import com.protonmail.landrevillejf.swingide.core.bus.EventBus;
import com.protonmail.landrevillejf.swingide.core.bus.events.PanelAddRequest;
import com.protonmail.landrevillejf.swingide.core.bus.events.PanelAddResponse;
import com.protonmail.landrevillejf.swingide.core.bus.events.PanelRemoveRequest;
import com.protonmail.landrevillejf.swingide.core.layout.IdePanelRegion;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Utilitaire pour que les plugins ajoutent facilement des panneaux
 */
@Slf4j
public class PanelUtil {

    private final EventBus eventBus;
    private final String pluginId;

    public PanelUtil(EventBus eventBus, String pluginId) {
        this.eventBus = eventBus;
        this.pluginId = pluginId;
    }

    /**
     * Ajoute un panneau de façon asynchrone (String location, pour compatibilité)
     */
    public CompletableFuture<Boolean> addPanel(String title, Icon icon, JPanel panel, String location) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        String panelId = pluginId + "-" + System.currentTimeMillis();
        PanelAddRequest request = new PanelAddRequest(pluginId, title, icon, panel, location);

        // S'abonner à la réponse
        eventBus.subscribe(PanelAddResponse.class, response -> {
            if (response.getPluginId().equals(pluginId) &&
                    response.getPanelId().equals(request.getPanelId())) {
                future.complete(response.isSuccess());
                if (!response.isSuccess()) {
                    log.error("Failed to add panel: {}", response.getMessage());
                }
            }
        });

        // Publier la requête
        eventBus.publish(request);

        // Timeout après 5 secondes
        CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(() -> {
            if (!future.isDone()) {
                future.complete(false);
            }
        });

        return future;
    }

    /**
     * Ajoute un panneau de façon asynchrone en utilisant une région typée.
     */
    public CompletableFuture<Boolean> addPanel(String title, Icon icon, JPanel panel, IdePanelRegion region) {
        return addPanel(title, icon, panel, toLocation(region));
    }

    /**
     * Ajoute un panneau de façon synchrone (String location, pour compatibilité)
     */
    public boolean addPanelSync(String title, Icon icon, JPanel panel, String location) {
        try {
            return addPanel(title, icon, panel, location).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Error adding panel synchronously", e);
            return false;
        }
    }

    /**
     * Ajoute un panneau de façon synchrone en utilisant une région typée.
     */
    public boolean addPanelSync(String title, Icon icon, JPanel panel, IdePanelRegion region) {
        return addPanelSync(title, icon, panel, toLocation(region));
    }

    /**
     * Supprime un panneau
     */
    public void removePanel(String panelId) {
        eventBus.publish(new PanelRemoveRequest(pluginId, panelId));
    }

    /**
     * Supprime tous les panneaux de ce plugin (à implémenter ultérieurement si besoin)
     */
    public void removeAllPanels() {
        // Cette méthode nécessite que le PanelManager ait une méthode pour ça
        // ou on peut garder une liste locale des panelIds
    }

    private String toLocation(IdePanelRegion region) {
        if (region == null) {
            return "center";
        }
        return switch (region) {
            case LEFT -> "left";
            case RIGHT -> "right";
            case BOTTOM -> "bottom";
            case CENTER -> "center";
        };
    }
}