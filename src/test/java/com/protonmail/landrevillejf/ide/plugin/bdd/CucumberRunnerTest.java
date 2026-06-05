package com.protonmail.landrevillejf.ide.plugin.bdd;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Cucumber BDD test runner for the plugin-api module.
 *
 * <p>Run with: {@code ./gradlew :plugin-api:cucumberTest}</p>
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME,
        value = "com.protonmail.landrevillejf.swingide.plugin.bdd.steps")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty, html:build/reports/cucumber/cucumber-report.html, json:build/reports/cucumber/cucumber-report.json")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME,
        value = "not @ignore")
public class CucumberRunnerTest {
    // Intentionally empty — configuration via annotations
}

