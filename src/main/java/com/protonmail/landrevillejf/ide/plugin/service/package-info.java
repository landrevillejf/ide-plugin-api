/**
 * Plugin Services Package
 *
 * This package provides comprehensive services for plugin development, including:
 *
 * <h2>Core Services</h2>
 * <ul>
 *   <li>{@link com.protonmail.landrevillejf.ide.plugin.service.PluginLoggingService} - Centralized logging</li>
 *   <li>{@link com.protonmail.landrevillejf.ide.plugin.service.PluginCacheService} - In-memory caching</li>
 *   <li>{@link com.protonmail.landrevillejf.ide.plugin.service.PluginNotificationService} - User notifications</li>
 *   <li>{@link com.protonmail.landrevillejf.ide.plugin.service.PluginMetricsService} - Performance metrics</li>
 * </ul>
 *
 * <h2>Management Services</h2>
 * <ul>
 *   <li>{@link com.protonmail.landrevillejf.ide.plugin.service.PluginPermissionService} - RBAC and permissions</li>
 *   <li>{@link com.protonmail.landrevillejf.ide.plugin.service.PluginResourceManager} - Cross-plugin resources</li>
 *   <li>{@link com.protonmail.landrevillejf.ide.plugin.service.PluginDependencyResolver} - Dependency management</li>
 *   <li>{@link com.protonmail.landrevillejf.ide.plugin.service.PluginDataStore} - Persistent storage</li>
 * </ul>
 *
 * <h2>Execution Services</h2>
 * <ul>
 *   <li>{@link com.protonmail.landrevillejf.ide.plugin.service.PluginAsyncTaskExecutor} - Async task execution</li>
 *   <li>{@link com.protonmail.landrevillejf.ide.plugin.service.PluginHookService} - Lifecycle hooks</li>
 * </ul>
 *
 * <h2>Validation - Monitoring</h2>
 * <ul>
 *   <li>{@link com.protonmail.landrevillejf.ide.plugin.service.PluginConfigurationValidator} - Config validation</li>
 *   <li>{@link com.protonmail.landrevillejf.ide.plugin.service.PluginUpdateService} - Update management</li>
 *   <li>{@link com.protonmail.landrevillejf.ide.plugin.service.PluginMonitoringService} - Health monitoring</li>
 *   <li>{@link com.protonmail.landrevillejf.ide.plugin.service.PluginLifecycleListener} - Lifecycle events</li>
 * </ul>
 *
 * <h2>Service Access</h2>
 * <p>Services are accessed through the {@link com.protonmail.landrevillejf.ide.plugin.service.PluginServiceLocator}
 * interface, which is available through {@link com.protonmail.landrevillejf.ide.plugin.ExtendedPluginContext}:</p>
 *
 * <pre>
 * if (context instanceof ExtendedPluginContext) {
 *     ExtendedPluginContext extContext = (ExtendedPluginContext) context;
 *     PluginLoggingService logger = extContext.getLoggingService();
 *     PluginCacheService cache = extContext.getCacheService();
 *     // ... use services
 * }
 * </pre>
 *
 * <h2>Service Configuration</h2>
 * <p>Services can be configured using {@link com.protonmail.landrevillejf.ide.plugin.service.PluginServiceConfiguration}
 * with a fluent builder API:</p>
 *
 * <pre>
 * PluginServiceLocator locator = PluginServiceConfiguration.builder()
 *     .withLoggingService(customLogger)
 *     .withCacheService(customCache)
 *     .build();
 * </pre>
 *
 * @see com.protonmail.landrevillejf.ide.plugin.ExtendedPluginContext
 * @see com.protonmail.landrevillejf.ide.plugin.service.PluginServiceLocator
 * @see com.protonmail.landrevillejf.ide.plugin.service.PluginServiceConfiguration
 */
package com.protonmail.landrevillejf.ide.plugin.service;

