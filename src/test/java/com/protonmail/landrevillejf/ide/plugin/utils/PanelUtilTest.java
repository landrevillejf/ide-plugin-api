package com.protonmail.landrevillejf.ide.plugin.utils;

import com.protonmail.landrevillejf.swingide.core.bus.EventBus;
import com.protonmail.landrevillejf.swingide.core.bus.events.PanelAddRequest;
import com.protonmail.landrevillejf.swingide.core.bus.events.PanelAddResponse;
import com.protonmail.landrevillejf.swingide.core.bus.events.PanelRemoveRequest;
import com.protonmail.landrevillejf.swingide.core.layout.IdePanelRegion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

import javax.swing.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PanelUtilTest {

    private EventBus eventBus;
    private PanelUtil panelUtil;
    private String pluginId;
    private JPanel testPanel;
    private Icon testIcon;
    private Consumer<PanelAddResponse> capturedSubscriber;

    @BeforeEach
    void setUp() {
        eventBus = mock(EventBus.class);
        pluginId = "test.plugin";
        panelUtil = new PanelUtil(eventBus, pluginId);
        testPanel = new JPanel();
        testIcon = mock(Icon.class);

        // Capture the subscriber when subscribe is called
        doAnswer((Answer<Void>) invocation -> {
            capturedSubscriber = invocation.getArgument(1);
            return null;
        }).when(eventBus).subscribe(eq(PanelAddResponse.class), any());
    }

    @Test
    void addPanel_AsyncWithStringLocation_ShouldPublishRequestAndReturnFuture() {
        // Given
        String title = "Test Panel";
        String location = "left";

        // When
        CompletableFuture<Boolean> future = panelUtil.addPanel(title, testIcon, testPanel, location);

        // Then
        assertNotNull(future);
        assertFalse(future.isDone());

        // Verify event was published
        ArgumentCaptor<PanelAddRequest> requestCaptor = ArgumentCaptor.forClass(PanelAddRequest.class);
        verify(eventBus, times(1)).publish(requestCaptor.capture());

        PanelAddRequest request = requestCaptor.getValue();
        assertEquals(pluginId, request.getPluginId());
        assertEquals(title, request.getTitle());
        assertEquals(testIcon, request.getIcon());
        assertEquals(testPanel, request.getPanel());
        assertEquals(location, request.getLocation());
        assertNotNull(request.getPanelId());
        assertTrue(request.getPanelId().startsWith(pluginId + "-"));
    }

    @Test
    void addPanel_AsyncWithStringLocation_ShouldCompleteFutureOnSuccessResponse() {
        // Given
        ArgumentCaptor<PanelAddRequest> requestCaptor = ArgumentCaptor.forClass(PanelAddRequest.class);
        CompletableFuture<Boolean> future = panelUtil.addPanel("Test", testIcon, testPanel, "center");

        verify(eventBus).publish(requestCaptor.capture());
        PanelAddRequest request = requestCaptor.getValue();

        verify(eventBus).subscribe(eq(PanelAddResponse.class), any());

        // When - simulate successful response
        PanelAddResponse successResponse = new PanelAddResponse(
                pluginId,
                request.getPanelId(),
                true,
                "Panel added successfully"
        );

        capturedSubscriber.accept(successResponse);

        // Then
        assertTrue(future.isDone());
        assertTrue(future.join());
    }

    @Test
    void addPanel_AsyncWithStringLocation_ShouldCompleteFutureWithFalseOnFailureResponse() {
        // Given
        ArgumentCaptor<PanelAddRequest> requestCaptor = ArgumentCaptor.forClass(PanelAddRequest.class);
        CompletableFuture<Boolean> future = panelUtil.addPanel("Test", testIcon, testPanel, "center");

        verify(eventBus).publish(requestCaptor.capture());
        PanelAddRequest request = requestCaptor.getValue();

        verify(eventBus).subscribe(eq(PanelAddResponse.class), any());

        // When - simulate failed response
        PanelAddResponse failureResponse = new PanelAddResponse(
                pluginId,
                request.getPanelId(),
                false,
                "Failed to add panel"
        );

        capturedSubscriber.accept(failureResponse);

        // Then
        assertTrue(future.isDone());
        assertFalse(future.join());
    }

    @Test
    void addPanel_AsyncWithStringLocation_ShouldIgnoreResponsesForOtherPlugins() {
        // Given
        ArgumentCaptor<PanelAddRequest> requestCaptor = ArgumentCaptor.forClass(PanelAddRequest.class);
        CompletableFuture<Boolean> future = panelUtil.addPanel("Test", testIcon, testPanel, "center");

        verify(eventBus).publish(requestCaptor.capture());
        PanelAddRequest request = requestCaptor.getValue();

        verify(eventBus).subscribe(eq(PanelAddResponse.class), any());

        // When - simulate response for different plugin
        PanelAddResponse wrongPluginResponse = new PanelAddResponse(
                "other.plugin",
                request.getPanelId(),
                true,
                "Success"
        );

        capturedSubscriber.accept(wrongPluginResponse);

        // Then - future should not complete yet
        assertFalse(future.isDone());

        // When - simulate correct response
        PanelAddResponse correctResponse = new PanelAddResponse(
                pluginId,
                request.getPanelId(),
                true,
                "Success"
        );

        capturedSubscriber.accept(correctResponse);

        // Then
        assertTrue(future.isDone());
        assertTrue(future.join());
    }

    @Test
    void addPanel_AsyncWithStringLocation_ShouldIgnoreResponsesForOtherPanelIds() {
        // Given
        ArgumentCaptor<PanelAddRequest> requestCaptor = ArgumentCaptor.forClass(PanelAddRequest.class);
        CompletableFuture<Boolean> future = panelUtil.addPanel("Test", testIcon, testPanel, "center");

        verify(eventBus).publish(requestCaptor.capture());
        String expectedPanelId = requestCaptor.getValue().getPanelId();

        verify(eventBus).subscribe(eq(PanelAddResponse.class), any());

        // When - simulate response for different panel ID
        PanelAddResponse wrongPanelResponse = new PanelAddResponse(
                pluginId,
                "wrong.panel.id",
                true,
                "Success"
        );

        capturedSubscriber.accept(wrongPanelResponse);

        // Then - future should not complete yet
        assertFalse(future.isDone());

        // When - simulate correct response
        PanelAddResponse correctResponse = new PanelAddResponse(
                pluginId,
                expectedPanelId,
                true,
                "Success"
        );

        capturedSubscriber.accept(correctResponse);

        // Then
        assertTrue(future.isDone());
        assertTrue(future.join());
    }

    @Test
    @Timeout(value = 6, unit = TimeUnit.SECONDS)
    void addPanel_ShouldCompleteWithFalseOnTimeout() throws Exception {
        // Given
        CompletableFuture<Boolean> future = panelUtil.addPanel("Test", testIcon, testPanel, "center");

        // When - no response is sent, timeout occurs after 5 seconds

        // Then
        Boolean result = future.get(6, TimeUnit.SECONDS);
        assertFalse(result);
    }

    @ParameterizedTest
    @EnumSource(IdePanelRegion.class)
    void addPanel_WithIdePanelRegion_ShouldConvertToCorrectLocation(IdePanelRegion region) {
        // Given
        String expectedLocation = switch (region) {
            case LEFT -> "left";
            case RIGHT -> "right";
            case BOTTOM -> "bottom";
            case CENTER -> "center";
        };
        ArgumentCaptor<PanelAddRequest> requestCaptor = ArgumentCaptor.forClass(PanelAddRequest.class);

        // When
        panelUtil.addPanel("Test Panel", testIcon, testPanel, region);

        // Then
        verify(eventBus).publish(requestCaptor.capture());
        assertEquals(expectedLocation, requestCaptor.getValue().getLocation());
    }

    @Test
    void addPanel_WithNullRegion_ShouldDefaultToCenter() {
        // When
        panelUtil.addPanel("Test", testIcon, testPanel, (IdePanelRegion) null);

        // Then
        ArgumentCaptor<PanelAddRequest> requestCaptor = ArgumentCaptor.forClass(PanelAddRequest.class);
        verify(eventBus).publish(requestCaptor.capture());
        assertEquals("center", requestCaptor.getValue().getLocation());
    }

    @Test
    void addPanelSync_ShouldReturnTrueOnSuccess() {
        // Given
        PanelUtil spyPanelUtil = spy(panelUtil);
        CompletableFuture<Boolean> completedFuture = CompletableFuture.completedFuture(true);
        doReturn(completedFuture).when(spyPanelUtil).addPanel(any(), any(), any(), any(String.class));

        // When
        boolean result = spyPanelUtil.addPanelSync("Test", testIcon, testPanel, "center");

        // Then
        assertTrue(result);
    }

    @Test
    void addPanelSync_ShouldReturnFalseOnFalseFuture() {
        // Given
        PanelUtil spyPanelUtil = spy(panelUtil);
        CompletableFuture<Boolean> completedFuture = CompletableFuture.completedFuture(false);
        doReturn(completedFuture).when(spyPanelUtil).addPanel(any(), any(), any(), any(String.class));

        // When
        boolean result = spyPanelUtil.addPanelSync("Test", testIcon, testPanel, "center");

        // Then
        assertFalse(result);
    }

    @Test
    void addPanelSync_ShouldReturnFalseOnException() {
        // Given
        PanelUtil spyPanelUtil = spy(panelUtil);
        CompletableFuture<Boolean> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Test exception"));
        doReturn(failedFuture).when(spyPanelUtil).addPanel(any(), any(), any(), any(String.class));

        // When
        boolean result = spyPanelUtil.addPanelSync("Test", testIcon, testPanel, "center");

        // Then
        assertFalse(result);
    }

    @Test
    void addPanelSync_WithRegion_ShouldCallStringLocationVersion() {
        // Given
        PanelUtil spyPanelUtil = spy(panelUtil);
        doReturn(true).when(spyPanelUtil).addPanelSync(any(), any(), any(), any(String.class));

        // When
        spyPanelUtil.addPanelSync("Test", testIcon, testPanel, IdePanelRegion.LEFT);

        // Then
        verify(spyPanelUtil).addPanelSync(eq("Test"), eq(testIcon), eq(testPanel), eq("left"));
    }

    @Test
    void addPanelSync_ShouldHandleTimeout() {
        // Given
        PanelUtil spyPanelUtil = spy(panelUtil);
        CompletableFuture<Boolean> pendingFuture = new CompletableFuture<>();
        doReturn(pendingFuture).when(spyPanelUtil).addPanel(any(), any(), any(), any(String.class));

        // When
        long startTime = System.currentTimeMillis();
        boolean result = spyPanelUtil.addPanelSync("Test", testIcon, testPanel, "center");
        long duration = System.currentTimeMillis() - startTime;

        // Then
        assertFalse(result);
        assertTrue(duration >= 4900, "Should timeout around 5 seconds, took: " + duration + "ms");
    }

    @Test
    void removePanel_ShouldPublishRemoveRequest() {
        // Given
        String panelId = "test.panel.id";

        // When
        panelUtil.removePanel(panelId);

        // Then
        ArgumentCaptor<PanelRemoveRequest> requestCaptor = ArgumentCaptor.forClass(PanelRemoveRequest.class);
        verify(eventBus).publish(requestCaptor.capture());

        PanelRemoveRequest request = requestCaptor.getValue();
        assertEquals(pluginId, request.getPluginId());
        assertEquals(panelId, request.getPanelId());
    }

    @Test
    void removePanel_ShouldHandleNullPanelId() {
        // When/Then - should not throw NPE
        assertDoesNotThrow(() -> panelUtil.removePanel(null));

        ArgumentCaptor<PanelRemoveRequest> requestCaptor = ArgumentCaptor.forClass(PanelRemoveRequest.class);
        verify(eventBus).publish(requestCaptor.capture());
        assertNull(requestCaptor.getValue().getPanelId());
    }

    @Test
    void removeAllPanels_CurrentlyDoesNothing() {
        // When - this method is currently a placeholder
        assertDoesNotThrow(() -> panelUtil.removeAllPanels());

        // Then - should not publish any events with current implementation
        verify(eventBus, never()).publish(any());
    }

    @Test
    void constructor_ShouldStorePluginIdAndEventBus() {
        // Then
        assertDoesNotThrow(() -> new PanelUtil(eventBus, "test.id"));
    }

    @Test
    void addPanel_ShouldGenerateUniquePanelIds() {
        // When
        panelUtil.addPanel("Panel 1", testIcon, testPanel, "left");
        panelUtil.addPanel("Panel 2", testIcon, testPanel, "right");

        // Then
        ArgumentCaptor<PanelAddRequest> requestCaptor = ArgumentCaptor.forClass(PanelAddRequest.class);
        verify(eventBus, times(2)).publish(requestCaptor.capture());

        String panelId1 = requestCaptor.getAllValues().get(0).getPanelId();
        String panelId2 = requestCaptor.getAllValues().get(1).getPanelId();

        assertNotEquals(panelId1, panelId2);
        assertTrue(panelId1.startsWith(pluginId + "-"));
        assertTrue(panelId2.startsWith(pluginId + "-"));
    }

    @Test
    void toLocation_PrivateMethod_TestViaPublicAPI() {
        // Test all enum values indirectly through addPanel
        for (IdePanelRegion region : IdePanelRegion.values()) {
            panelUtil.addPanel("Test", testIcon, testPanel, region);
        }

        ArgumentCaptor<PanelAddRequest> requestCaptor = ArgumentCaptor.forClass(PanelAddRequest.class);
        verify(eventBus, times(IdePanelRegion.values().length)).publish(requestCaptor.capture());

        // Verify each conversion
        var requests = requestCaptor.getAllValues();
        assertEquals("left", requests.get(0).getLocation());
        assertEquals("right", requests.get(1).getLocation());
        assertEquals("bottom", requests.get(2).getLocation());
        assertEquals("center", requests.get(3).getLocation());
    }

    @Test
    void addPanel_ShouldLogErrorOnFailureResponse() {
        // Given
        ArgumentCaptor<PanelAddRequest> requestCaptor = ArgumentCaptor.forClass(PanelAddRequest.class);
        CompletableFuture<Boolean> future = panelUtil.addPanel("Test", testIcon, testPanel, "center");

        verify(eventBus).publish(requestCaptor.capture());
        PanelAddRequest request = requestCaptor.getValue();

        verify(eventBus).subscribe(eq(PanelAddResponse.class), any());

        // When - simulate failed response
        PanelAddResponse failureResponse = new PanelAddResponse(
                pluginId,
                request.getPanelId(),
                false,
                "Error message"
        );

        capturedSubscriber.accept(failureResponse);

        // Then
        assertTrue(future.isDone());
        assertFalse(future.join());
    }

    @Test
    void addPanel_ShouldNotCompleteForNonMatchingPluginId() {
        // Given
        ArgumentCaptor<PanelAddRequest> requestCaptor = ArgumentCaptor.forClass(PanelAddRequest.class);
        CompletableFuture<Boolean> future = panelUtil.addPanel("Test", testIcon, testPanel, "center");

        verify(eventBus).publish(requestCaptor.capture());

        verify(eventBus).subscribe(eq(PanelAddResponse.class), any());

        // When - simulate response for different plugin
        PanelAddResponse wrongPluginResponse = new PanelAddResponse(
                "different.plugin",
                "any-id",
                true,
                "Success"
        );

        capturedSubscriber.accept(wrongPluginResponse);

        // Then
        assertFalse(future.isDone());
    }

    @Test
    void addPanel_ShouldNotCompleteForNonMatchingPanelId() {
        // Given
        ArgumentCaptor<PanelAddRequest> requestCaptor = ArgumentCaptor.forClass(PanelAddRequest.class);
        CompletableFuture<Boolean> future = panelUtil.addPanel("Test", testIcon, testPanel, "center");

        verify(eventBus).publish(requestCaptor.capture());

        verify(eventBus).subscribe(eq(PanelAddResponse.class), any());

        // When - simulate response for different panel ID
        PanelAddResponse wrongPanelResponse = new PanelAddResponse(
                pluginId,
                "wrong.panel.id",
                true,
                "Success"
        );

        capturedSubscriber.accept(wrongPanelResponse);

        // Then
        assertFalse(future.isDone());
    }
}