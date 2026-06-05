package com.protonmail.landrevillejf.swingide.plugin.bdd.steps;

import com.protonmail.landrevillejf.swingide.plugin.PluginDescriptor;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for PluginDescriptor BDD scenarios.
 */
public class PluginDescriptorSteps {

    private PluginDescriptor descriptor;
    private PluginDescriptor copy;

    @Given("a descriptor with id {string} name {string} version {string} mainClass {string} description {string} author {string}")
    @When("I create a descriptor with id {string} name {string} version {string} mainClass {string} description {string} author {string}")
    public void iCreateADescriptor(String id, String name, String version,
                                   String mainClass, String description, String author) {
        descriptor = new PluginDescriptor(id, name, version, mainClass, description, author);
    }

    @When("I copy the descriptor")
    public void iCopyTheDescriptor() {
        copy = descriptor.copy();
    }

    @When("I set providesMenu to true")
    public void iSetProvidesMenuToTrue() {
        descriptor.setProvidesMenu(true);
    }

    @When("I set providesToolbar to true")
    public void iSetProvidesToolbarToTrue() {
        descriptor.setProvidesToolbar(true);
    }

    @When("I set requiresNetwork to true")
    public void iSetRequiresNetworkToTrue() {
        descriptor.setRequiresNetwork(true);
    }

    // ── Basic field assertions ──

    @Then("the descriptor id should be {string}")
    public void theDescriptorIdShouldBe(String expected) {
        assertEquals(expected, descriptor.getId());
    }

    @Then("the descriptor name should be {string}")
    public void theDescriptorNameShouldBe(String expected) {
        assertEquals(expected, descriptor.getName());
    }

    @Then("the descriptor version should be {string}")
    public void theDescriptorVersionShouldBe(String expected) {
        assertEquals(expected, descriptor.getVersion());
    }

    @Then("the descriptor mainClass should be {string}")
    public void theDescriptorMainClassShouldBe(String expected) {
        assertEquals(expected, descriptor.getMainClass());
    }

    @Then("the descriptor description should be {string}")
    public void theDescriptorDescriptionShouldBe(String expected) {
        assertEquals(expected, descriptor.getDescription());
    }

    @Then("the descriptor author should be {string}")
    public void theDescriptorAuthorShouldBe(String expected) {
        assertEquals(expected, descriptor.getAuthor());
    }

    // ── Default values ──

    @Then("the descriptor category should be {string}")
    public void theDescriptorCategoryShouldBe(String expected) {
        assertEquals(expected, descriptor.getCategory());
    }

    @Then("the descriptor requiredHostVersion should be {string}")
    public void theDescriptorRequiredHostVersionShouldBe(String expected) {
        assertEquals(expected, descriptor.getRequiredHostVersion());
    }

    @Then("the descriptor should not be enabled by default")
    public void theDescriptorShouldNotBeEnabledByDefault() {
        assertFalse(descriptor.isEnabledByDefault());
    }

    @Then("the descriptor should not auto-start")
    public void theDescriptorShouldNotAutoStart() {
        assertFalse(descriptor.isAutoStart());
    }

    // ── Copy assertions ──

    @Then("the copy should have id {string}")
    public void theCopyShouldHaveId(String expected) {
        assertNotNull(copy);
        assertEquals(expected, copy.getId());
    }

    @Then("the copy should have name {string}")
    public void theCopyShouldHaveName(String expected) {
        assertNotNull(copy);
        assertEquals(expected, copy.getName());
    }

    @Then("the copy should have version {string}")
    public void theCopyShouldHaveVersion(String expected) {
        assertNotNull(copy);
        assertEquals(expected, copy.getVersion());
    }

    // ── Capabilities ──

    @Then("the descriptor should provide menu")
    public void theDescriptorShouldProvideMenu() {
        assertTrue(descriptor.isProvidesMenu());
    }

    @Then("the descriptor should provide toolbar")
    public void theDescriptorShouldProvideToolbar() {
        assertTrue(descriptor.isProvidesToolbar());
    }

    @Then("the descriptor should require network")
    public void theDescriptorShouldRequireNetwork() {
        assertTrue(descriptor.isRequiresNetwork());
    }
}

