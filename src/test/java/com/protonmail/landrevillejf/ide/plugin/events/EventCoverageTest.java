package com.protonmail.landrevillejf.ide.plugin.events;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class EventCoverageTest {

    static Stream<Arguments> eventProvider() {
        return Stream.of(
                Arguments.of(new BuildStartedEvent("p", "/path", "MAVEN", "compile")),
                Arguments.of(new EditorTextChangedEvent("p", "editor", "/file", 1, 2, 3, "text")),
                Arguments.of(new FileOpenedEvent("p", "/file")),
                Arguments.of(new MenuItemClickedEvent("p", "menu", "item", "text")),
                Arguments.of(new PluginLoadedEvent("p", "id", "name", "1.0")),
                Arguments.of(new PluginUnloadedEvent("p", "id", "name")),
                Arguments.of(new ProjectClosedEvent("p", "/path", "name")),
                Arguments.of(new ProjectCreatedEvent("p", "/path", "name", "type")),
                Arguments.of(new RunStartedEvent("p", "/path", "config")),
                Arguments.of(new TabClosedEvent("p", "id", "title")),
                Arguments.of(new TabOpenedEvent("p", "id", "title", "/file", "type")),
                Arguments.of(new TabSelectedEvent("p", "id", "title"))
        );
    }

    @ParameterizedTest
    @MethodSource("eventProvider")
    void testAllEvents(Event event) {
        assertNotNull(event);
        assertNotNull(event.getSource());
        assertNotNull(event.getTimestamp());
    }

    @ParameterizedTest
    @MethodSource("eventProvider")
    void testToString(Event event) {
        assertNotNull(event.toString());
    }
}