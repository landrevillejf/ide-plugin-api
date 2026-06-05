package com.protonmail.landrevillejf.ide.plugin.bdd.steps;

import com.protonmail.landrevillejf.ide.plugin.PluginConfig;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for PluginConfig BDD scenarios.
 */
public class PluginConfigSteps {

    private PluginConfig config;

    @Given("a new PluginConfig")
    public void aNewPluginConfig() {
        config = new PluginConfig();
    }

    // ── Auto-enable ──

    @When("I set auto-enable to true")
    public void iSetAutoEnableToTrue() {
        config.setAutoEnable(true);
    }

    @Then("auto-enable should be true")
    public void autoEnableShouldBeTrue() {
        assertTrue(config.isAutoEnable());
    }

    @Then("auto-enable should be false")
    public void autoEnableShouldBeFalse() {
        assertFalse(config.isAutoEnable());
    }

    // ── Settings ──

    @When("I set setting {string} to {string}")
    public void iSetSettingTo(String key, String value) {
        config.setSetting(key, value);
    }

    @When("I remove setting {string}")
    public void iRemoveSetting(String key) {
        config.removeSetting(key);
    }

    @Then("the setting {string} should be {string}")
    public void theSettingShouldBe(String key, String expected) {
        Object actual = config.getSetting(key);
        assertNotNull(actual, "Setting '" + key + "' should exist");
        assertEquals(expected, actual.toString());
    }

    @Then("the setting {string} with default {string} should be {string}")
    public void theSettingWithDefaultShouldBe(String key, String defaultVal, String expected) {
        Object actual = config.getSetting(key, defaultVal);
        assertEquals(expected, actual.toString());
    }

    @Then("the config should have setting {string}")
    public void theConfigShouldHaveSetting(String key) {
        assertTrue(config.hasSetting(key), "Config should have setting '" + key + "'");
    }

    @Then("the config should not have setting {string}")
    public void theConfigShouldNotHaveSetting(String key) {
        assertFalse(config.hasSetting(key), "Config should not have setting '" + key + "'");
    }

    @Then("the boolean setting {string} with default {} should be {}")
    public void theBooleanSettingWithDefaultShouldBe(String key, boolean defaultVal, boolean expected) {
        assertEquals(expected, config.getSettingAsBoolean(key, defaultVal));
    }

    @Then("the int setting {string} with default {int} should be {int}")
    public void theIntSettingWithDefaultShouldBe(String key, int defaultVal, int expected) {
        assertEquals(expected, config.getSettingAsInt(key, defaultVal));
    }

    // ── Features ──

    @When("I enable feature {string}")
    public void iEnableFeature(String feature) {
        config.enableFeature(feature);
    }

    @When("I disable feature {string}")
    public void iDisableFeature(String feature) {
        config.disableFeature(feature);
    }

    @Then("the enabled features should contain {string}")
    public void theEnabledFeaturesShouldContain(String feature) {
        assertTrue(config.getEnabledFeatures().contains(feature),
                "Enabled features should contain '" + feature + "'");
    }

    @Then("the enabled features should not contain {string}")
    public void theEnabledFeaturesShouldNotContain(String feature) {
        assertFalse(config.getEnabledFeatures().contains(feature),
                "Enabled features should not contain '" + feature + "'");
    }

    @Then("the feature {string} should be enabled")
    public void theFeatureShouldBeEnabled(String feature) {
        assertTrue(config.isFeatureEnabled(feature));
    }

    @Then("the feature {string} should not be enabled")
    public void theFeatureShouldNotBeEnabled(String feature) {
        assertFalse(config.isFeatureEnabled(feature));
    }
}

