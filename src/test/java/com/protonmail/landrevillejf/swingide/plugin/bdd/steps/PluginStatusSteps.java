package com.protonmail.landrevillejf.swingide.plugin.bdd.steps;

import com.protonmail.landrevillejf.swingide.plugin.PluginStatus;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for PluginStatus BDD scenarios.
 */
public class PluginStatusSteps {

    private PluginStatus status;

    @Given("a plugin status {word}")
    public void aPluginStatus(String statusName) {
        status = PluginStatus.valueOf(statusName);
    }

    @Then("the status should be active")
    public void theStatusShouldBeActive() {
        assertTrue(status.isActive(), status + " should be active");
    }

    @Then("the status should not be active")
    public void theStatusShouldNotBeActive() {
        assertFalse(status.isActive(), status + " should not be active");
    }

    @Then("the status should be inactive")
    public void theStatusShouldBeInactive() {
        assertTrue(status.isInactive(), status + " should be inactive");
    }

    @Then("the status should not be inactive")
    public void theStatusShouldNotBeInactive() {
        assertFalse(status.isInactive(), status + " should not be inactive");
    }

    @Then("the status description should be {string}")
    public void theStatusDescriptionShouldBe(String expected) {
        assertEquals(expected, status.toString());
    }

    @Then("it should be allowed to transition to {word}")
    public void itShouldBeAllowedToTransitionTo(String targetName) {
        PluginStatus target = PluginStatus.valueOf(targetName);
        assertTrue(status.canTransitionTo(target),
                status + " should be allowed to transition to " + target);
    }

    @Then("it should not be allowed to transition to {word}")
    public void itShouldNotBeAllowedToTransitionTo(String targetName) {
        PluginStatus target = PluginStatus.valueOf(targetName);
        assertFalse(status.canTransitionTo(target),
                status + " should NOT be allowed to transition to " + target);
    }
}

