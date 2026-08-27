package com.protonmail.landrevillejf.ide.plugin.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Test utility that captures log events emitted by a specific logger.
 * <p>
 * Usage:
 * <pre>
 * try (LogCapture capture = LogCapture.attach(DefaultPluginPermissionService.class)) {
 *     // exercise code
 *     assertThat(capture.formattedMessages()).contains("...");
 * }
 * </pre>
 * </p>
 */
public final class LogCapture implements AutoCloseable {

    private final Logger logger;
    private final ListAppender<ILoggingEvent> appender;
    private final Level originalLevel;

    private LogCapture(Logger logger, ListAppender<ILoggingEvent> appender, Level originalLevel) {
        this.logger = logger;
        this.appender = appender;
        this.originalLevel = originalLevel;
    }

    /**
     * Attaches a capturing appender to the logger of the given class.
     *
     * @param owner the class whose logger should be captured
     * @return an active capture, detachable via {@link #close()}
     */
    public static LogCapture attach(Class<?> owner) {
        Logger logger = (Logger) LoggerFactory.getLogger(owner);
        Level originalLevel = logger.getLevel();
        logger.setLevel(Level.TRACE);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return new LogCapture(logger, appender, originalLevel);
    }

    /**
     * Returns all captured logging events.
     *
     * @return the captured events
     */
    public List<ILoggingEvent> events() {
        return appender.list;
    }

    /**
     * Returns the formatted messages of all captured events.
     *
     * @return the formatted messages
     */
    public List<String> formattedMessages() {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
    }

    /**
     * Returns the levels of all captured events.
     *
     * @return the event levels
     */
    public List<Level> levels() {
        return appender.list.stream().map(ILoggingEvent::getLevel).toList();
    }

    @Override
    public void close() {
        logger.detachAppender(appender);
        logger.setLevel(originalLevel);
        appender.stop();
    }
}
