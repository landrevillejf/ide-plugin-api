package com.protonmail.landrevillejf.swingide.plugin.cucumber;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Cucumber test runner for Plugin API BDD tests
 */
@Suite
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "io.cucumber.core.plugin.FormatterOptions:pretty,html:target/cucumber-report.html")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.protonmail.landrevillejf.swingide.plugin.cucumber")
@ConfigurationParameter(key = FEATURES_PROPERTY_NAME, value = "src/test/resources/features")
public class PluginServicesRunnerTests {
    // Cucumber tests will run through this class
}

