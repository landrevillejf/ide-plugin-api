package com.protonmail.landrevillejf.ide.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mutation-killing tests for {@link PluginConfig}.
 */
@DisplayName("PluginConfig mutation tests")
class PluginConfigMutationTest {

    private PluginConfig config;

    @BeforeEach
    void setUp() {
        config = new PluginConfig();
    }

    private void pinLastModified() {
        config.setMetadata("lastModified", 0L);
    }

    private Long lastModified() {
        return (Long) config.getMetadata().get("lastModified");
    }

    @Nested
    @DisplayName("typed setting accessors")
    class TypedAccessorTests {

        @Test
        @DisplayName("boolean settings honour stored Boolean values")
        void booleanSettings() {
            config.setSetting("on", Boolean.TRUE);
            config.setSetting("off", Boolean.FALSE);

            assertThat(config.getSettingAsBoolean("on", false)).isTrue();
            assertThat(config.getSettingAsBoolean("off", true)).isFalse();
        }

        @Test
        @DisplayName("boolean settings parse string values")
        void booleanStrings() {
            config.setSetting("yes", "true");
            config.setSetting("no", "false");
            config.setSetting("junk", "certainly");

            assertThat(config.getSettingAsBoolean("yes", false)).isTrue();
            assertThat(config.getSettingAsBoolean("no", true)).isFalse();
            assertThat(config.getSettingAsBoolean("junk", true)).isFalse();
        }

        @Test
        @DisplayName("boolean settings fall back to the default")
        void booleanDefault() {
            config.setSetting("number", 1);
            assertThat(config.getSettingAsBoolean("number", true)).isTrue();
            assertThat(config.getSettingAsBoolean("number", false)).isFalse();
            assertThat(config.getSettingAsBoolean("missing", true)).isTrue();
            assertThat(config.getSettingAsBoolean("missing", false)).isFalse();
        }

        @Test
        @DisplayName("int settings convert Integer, Number and String values")
        void intSettings() {
            config.setSetting("int", 42);
            config.setSetting("double", 7.9);
            config.setSetting("text", "13");
            config.setSetting("junk", "abc");
            config.setSetting("bool", Boolean.TRUE);

            assertThat(config.getSettingAsInt("int", 0)).isEqualTo(42);
            assertThat(config.getSettingAsInt("double", 0)).isEqualTo(7);
            assertThat(config.getSettingAsInt("text", 0)).isEqualTo(13);
            assertThat(config.getSettingAsInt("junk", -1)).isEqualTo(-1);
            assertThat(config.getSettingAsInt("bool", -2)).isEqualTo(-2);
            assertThat(config.getSettingAsInt("missing", -3)).isEqualTo(-3);
        }

        @Test
        @DisplayName("getSettings exposes the exact settings")
        void settingsView() {
            config.setSetting("a", 1);
            config.setSetting("b", "two");

            assertThat(config.getSettings())
                    .hasSize(2)
                    .containsEntry("a", 1)
                    .containsEntry("b", "two");
        }
    }

    @Nested
    @DisplayName("lastModified bookkeeping")
    class LastModifiedTests {

        @Test
        @DisplayName("setAutoEnable touches lastModified")
        void autoEnableTouches() {
            pinLastModified();
            config.setAutoEnable(true);
            assertThat(lastModified()).isNotZero();
        }

        @Test
        @DisplayName("setSetting touches lastModified")
        void setSettingTouches() {
            pinLastModified();
            config.setSetting("k", "v");
            assertThat(lastModified()).isNotZero();
        }

        @Test
        @DisplayName("enableFeature touches lastModified only for new features")
        void enableFeatureTouches() {
            config.enableFeature("feat");
            pinLastModified();
            config.enableFeature("feat"); // duplicate: no change
            assertThat(lastModified()).isZero();
            config.enableFeature("other");
            assertThat(lastModified()).isNotZero();
            assertThat(config.getEnabledFeatures()).containsExactly("feat", "other");
        }

        @Test
        @DisplayName("disableFeature touches lastModified only when a feature was removed")
        void disableFeatureTouches() {
            config.enableFeature("feat");
            pinLastModified();
            config.disableFeature("ghost");
            assertThat(lastModified()).isZero();
            config.disableFeature("feat");
            assertThat(lastModified()).isNotZero();
            assertThat(config.isFeatureEnabled("feat")).isFalse();
        }

        @Test
        @DisplayName("removeSetting touches lastModified only when something was removed")
        void removeSettingTouches() {
            config.setSetting("k", "v");
            pinLastModified();
            assertThat(config.removeSetting("ghost")).isNull();
            assertThat(lastModified()).isZero();
            assertThat(config.removeSetting("k")).isEqualTo("v");
            assertThat(lastModified()).isNotZero();
            assertThat(config.hasSetting("k")).isFalse();
        }
    }

    @Nested
    @DisplayName("map serialization")
    class SerializationTests {

        @Test
        @DisplayName("fromMap replaces existing state completely")
        void fromMapReplaces() {
            config.setSetting("old", "stale");
            config.enableFeature("oldFeature");
            config.setMetadata("mk", "mv");

            Map<String, Object> map = new HashMap<>();
            map.put("autoEnable", Boolean.TRUE);
            map.put("settings", Map.of("new", 1));
            map.put("enabledFeatures", Arrays.asList(null, "f1"));
            map.put("metadata", Map.of("nk", 2));

            config.fromMap(map);

            assertThat(config.isAutoEnable()).isTrue();
            assertThat(config.getSettings())
                    .containsOnly(Map.entry("new", 1));
            assertThat(config.getEnabledFeatures()).containsExactly("f1");
            assertThat(config.getMetadata())
                    .containsEntry("nk", 2)
                    .doesNotContainKey("mk")
                    .doesNotContainKey("created")
                    .containsKey("lastLoaded");
        }

        @Test
        @DisplayName("fromMap ignores null maps and wrong-typed sections")
        void fromMapIgnores() {
            config.fromMap(null);

            Map<String, Object> map = new HashMap<>();
            map.put("autoEnable", "yes");          // not a Boolean
            map.put("settings", "not a map");
            map.put("enabledFeatures", "not a collection");
            map.put("metadata", "not a map");

            config.fromMap(map);

            assertThat(config.isAutoEnable()).isFalse();
            assertThat(config.getSettings()).isEmpty();
            assertThat(config.getEnabledFeatures()).isEmpty();
            assertThat(config.getMetadata()).containsKey("created");
        }

        @Test
        @DisplayName("toMap deep-copies every structure")
        void toMapDeepCopy() {
            List<Object> nestedList = new ArrayList<>(Arrays.asList("a", List.of("b")));
            Set<String> set = new LinkedHashSet<>(Set.of("x"));
            Map<String, Object> innerMap = new HashMap<>();
            innerMap.put("k", List.of("v"));

            config.setSetting("text", "s");
            config.setSetting("number", 5);
            config.setSetting("flag", true);
            config.setSetting("letter", 'c');
            config.setSetting("nil", null);
            config.setSetting("list", nestedList);
            config.setSetting("set", set);
            config.setSetting("map", innerMap);
            config.setSetting("custom", new Custom());

            @SuppressWarnings("unchecked")
            Map<String, Object> settings =
                    (Map<String, Object>) config.toMap().get("settings");

            assertThat(settings.get("text")).isEqualTo("s");
            assertThat(settings.get("number")).isEqualTo(5);
            assertThat(settings.get("flag")).isEqualTo(true);
            assertThat(settings.get("letter")).isEqualTo('c');
            assertThat(settings).containsEntry("nil", null);

            assertThat(settings.get("list")).isNotSameAs(nestedList);
            @SuppressWarnings("unchecked")
            List<Object> copiedList = (List<Object>) settings.get("list");
            assertThat(copiedList).containsExactly("a", List.of("b"));

            assertThat(settings.get("set")).isNotSameAs(set);
            @SuppressWarnings("unchecked")
            Set<String> copiedSet = (Set<String>) settings.get("set");
            assertThat(copiedSet).containsExactly("x");

            assertThat(settings.get("map")).isNotSameAs(innerMap);
            @SuppressWarnings("unchecked")
            Map<String, Object> copiedMap = (Map<String, Object>) settings.get("map");
            assertThat(copiedMap).containsEntry("k", List.of("v"));

            assertThat(settings.get("custom")).isEqualTo("custom-string");
        }

        @Test
        @DisplayName("map constructor loads via fromMap")
        void mapConstructor() {
            PluginConfig built = new PluginConfig(
                    Map.of("autoEnable", Boolean.TRUE, "settings", Map.of("k", "v")));
            assertThat(built.isAutoEnable()).isTrue();
            assertThat(built.getSettings()).containsEntry("k", "v");
        }
    }

    @Test
    @DisplayName("reset wipes everything back to defaults")
    void reset() {
        config.setAutoEnable(true);
        config.setSetting("k", "v");
        config.enableFeature("feat");
        config.setMetadata("custom", "value");

        config.reset();

        assertThat(config.isAutoEnable()).isFalse();
        assertThat(config.getSettings()).isEmpty();
        assertThat(config.getEnabledFeatures()).isEmpty();
        assertThat(config.getMetadata().keySet())
                .containsExactlyInAnyOrder("created", "version", "reset");
        assertThat(config.getMetadata()).containsEntry("reset", true);
    }

    private static final class Custom {
        @Override
        public String toString() {
            return "custom-string";
        }
    }
}
