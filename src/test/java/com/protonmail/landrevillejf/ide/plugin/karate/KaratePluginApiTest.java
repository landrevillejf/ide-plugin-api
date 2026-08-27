package com.protonmail.landrevillejf.ide.plugin.karate;

import com.intuit.karate.junit5.Karate;

/**
 * Karate BDD runner for the plugin API.
 * <p>
 * Karate scenarios exercise the public Java API through Karate's Java
 * interoperability ({@code Java.type}), providing behavior-driven coverage
 * complementary to the JUnit and Cucumber suites.
 * </p>
 */
class KaratePluginApiTest {

    @Karate.Test
    Karate testPluginLifecycle() {
        return Karate.run("plugin-lifecycle").relativeTo(getClass());
    }

    @Karate.Test
    Karate testPluginServices() {
        return Karate.run("plugin-services-karate").relativeTo(getClass());
    }

    @Karate.Test
    Karate testEventSystem() {
        return Karate.run("event-system").relativeTo(getClass());
    }
}
