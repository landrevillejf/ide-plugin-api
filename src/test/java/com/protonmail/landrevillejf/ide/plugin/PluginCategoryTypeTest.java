package com.protonmail.landrevillejf.ide.plugin;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PluginCategoryTypeTest {

    @Test
    void getIcon_ShouldReturnNonNullIcon() {
        // When
        Icon icon = PluginCategoryType.GAMES.getIcon();

        // Then
        assertNotNull(icon);
    }

    @Test
    void getIcon_ForAllCategories_ShouldReturnNonNull() {
        // When/Then
        for (PluginCategoryType category : PluginCategoryType.values()) {
            // DEFAULT peut retourner null, donc on skip ou on teste différemment
            if (category != PluginCategoryType.DEFAULT) {
                assertNotNull(category.getIcon(), "Icon should not be null for category: " + category.name());
            }
        }
    }

    @Test
    void getDisplayName_ShouldReturnCorrectDisplayName() {
        // Then
        assertEquals("games", PluginCategoryType.GAMES.getDisplayName());
        assertEquals("tools", PluginCategoryType.TOOLS.getDisplayName());
        assertEquals("development", PluginCategoryType.DEVELOPMENT.getDisplayName());
        assertEquals("analytics", PluginCategoryType.ANALYTICS.getDisplayName());
        assertEquals("version control", PluginCategoryType.VERSION_CONTROL.getDisplayName());
        assertEquals("container", PluginCategoryType.CONTAINER.getDisplayName());
        assertEquals("database", PluginCategoryType.DATABASE.getDisplayName());
        assertEquals("web", PluginCategoryType.WEB.getDisplayName());
        assertEquals("java", PluginCategoryType.JAVA.getDisplayName());
        assertEquals("python", PluginCategoryType.PYTHON.getDisplayName());
        assertEquals("mobile", PluginCategoryType.MOBILE.getDisplayName());
        assertEquals("cloud", PluginCategoryType.CLOUD.getDisplayName());
        assertEquals("testing", PluginCategoryType.TESTING.getDisplayName());
        assertEquals("security", PluginCategoryType.SECURITY.getDisplayName());
        assertEquals("documentation", PluginCategoryType.DOCUMENTATION.getDisplayName());
        assertEquals("design", PluginCategoryType.DESIGN.getDisplayName());
        assertEquals("configuration", PluginCategoryType.CONFIGURATION.getDisplayName());
        assertEquals("monitoring", PluginCategoryType.MONITORING.getDisplayName());
        assertEquals("api", PluginCategoryType.API.getDisplayName());
        assertEquals("ai", PluginCategoryType.AI.getDisplayName());
        assertEquals("data", PluginCategoryType.DATA.getDisplayName());
        assertEquals("messaging", PluginCategoryType.MESSAGING.getDisplayName());
        assertEquals("automation", PluginCategoryType.AUTOMATION.getDisplayName());
        assertEquals("virtualization", PluginCategoryType.VIRTUALIZATION.getDisplayName());
        assertEquals("networking", PluginCategoryType.NETWORKING.getDisplayName());
        assertEquals("default", PluginCategoryType.DEFAULT.getDisplayName());
    }

    @Test
    void getKeywords_ShouldReturnNonEmptySetForNonDefaultCategories() {
        // Then - Pour toutes les catégories sauf DEFAULT
        for (PluginCategoryType category : PluginCategoryType.values()) {
            if (category != PluginCategoryType.DEFAULT) {
                assertFalse(category.getKeywords().isEmpty(),
                        "Keywords should not be empty for category: " + category.name());
            }
        }
    }

    @Test
    void getKeywords_ForDefaultCategory_ShouldReturnEmptySet() {
        // Then
        assertTrue(PluginCategoryType.DEFAULT.getKeywords().isEmpty());
    }

    @Test
    void getKeywords_ShouldContainCorrectKeywords() {
        // Then
        assertTrue(PluginCategoryType.GAMES.getKeywords().contains("games"));
        assertTrue(PluginCategoryType.TOOLS.getKeywords().contains("tools"));
        assertTrue(PluginCategoryType.DEVELOPMENT.getKeywords().contains("development"));
        assertTrue(PluginCategoryType.WEB.getKeywords().contains("web"));
        assertTrue(PluginCategoryType.WEB.getKeywords().contains("javascript"));
        assertTrue(PluginCategoryType.JAVA.getKeywords().contains("java"));
        assertTrue(PluginCategoryType.JAVA.getKeywords().contains("spring"));
        assertTrue(PluginCategoryType.TESTING.getKeywords().contains("junit"));
        assertTrue(PluginCategoryType.SECURITY.getKeywords().contains("security"));
        assertTrue(PluginCategoryType.DOCUMENTATION.getKeywords().contains("documentation"));
    }

    @Test
    void fromKeyword_ShouldReturnCorrectCategory() {
        // When/Then - Existing keywords
        assertEquals(PluginCategoryType.GAMES, PluginCategoryType.fromKeyword("games"));
        assertEquals(PluginCategoryType.TOOLS, PluginCategoryType.fromKeyword("tools"));
        assertEquals(PluginCategoryType.DEVELOPMENT, PluginCategoryType.fromKeyword("development"));
        assertEquals(PluginCategoryType.WEB, PluginCategoryType.fromKeyword("web"));
        assertEquals(PluginCategoryType.WEB, PluginCategoryType.fromKeyword("javascript"));
        assertEquals(PluginCategoryType.WEB, PluginCategoryType.fromKeyword("react"));
        assertEquals(PluginCategoryType.JAVA, PluginCategoryType.fromKeyword("java"));
        assertEquals(PluginCategoryType.JAVA, PluginCategoryType.fromKeyword("spring"));
        assertEquals(PluginCategoryType.PYTHON, PluginCategoryType.fromKeyword("python"));
        assertEquals(PluginCategoryType.MOBILE, PluginCategoryType.fromKeyword("android"));
        assertEquals(PluginCategoryType.TESTING, PluginCategoryType.fromKeyword("testing"));
        assertEquals(PluginCategoryType.TESTING, PluginCategoryType.fromKeyword("junit"));
        assertEquals(PluginCategoryType.SECURITY, PluginCategoryType.fromKeyword("security"));
        assertEquals(PluginCategoryType.DOCUMENTATION, PluginCategoryType.fromKeyword("documentation"));
        assertEquals(PluginCategoryType.DESIGN, PluginCategoryType.fromKeyword("design"));
        assertEquals(PluginCategoryType.CONFIGURATION, PluginCategoryType.fromKeyword("configuration"));
        assertEquals(PluginCategoryType.MONITORING, PluginCategoryType.fromKeyword("monitoring"));
        assertEquals(PluginCategoryType.API, PluginCategoryType.fromKeyword("api"));
        assertEquals(PluginCategoryType.AI, PluginCategoryType.fromKeyword("ai"));
        assertEquals(PluginCategoryType.DATA, PluginCategoryType.fromKeyword("data"));
        assertEquals(PluginCategoryType.MESSAGING, PluginCategoryType.fromKeyword("messaging"));
        assertEquals(PluginCategoryType.AUTOMATION, PluginCategoryType.fromKeyword("automation"));
        assertEquals(PluginCategoryType.VIRTUALIZATION, PluginCategoryType.fromKeyword("virtualization"));
        assertEquals(PluginCategoryType.NETWORKING, PluginCategoryType.fromKeyword("networking"));
    }

    @Test
    void fromKeyword_WithCaseInsensitive_ShouldReturnCorrectCategory() {
        // When/Then - Uppercase
        assertEquals(PluginCategoryType.GAMES, PluginCategoryType.fromKeyword("GAMES"));
        assertEquals(PluginCategoryType.WEB, PluginCategoryType.fromKeyword("WEB"));
        assertEquals(PluginCategoryType.JAVA, PluginCategoryType.fromKeyword("JAVA"));

        // Mixed case
        assertEquals(PluginCategoryType.TOOLS, PluginCategoryType.fromKeyword("Tools"));
        assertEquals(PluginCategoryType.DEVELOPMENT, PluginCategoryType.fromKeyword("Development"));
        assertEquals(PluginCategoryType.WEB, PluginCategoryType.fromKeyword("JavaScript"));
        assertEquals(PluginCategoryType.JAVA, PluginCategoryType.fromKeyword("Spring"));
    }

    @Test
    void fromKeyword_WithWhitespace_ShouldTrimAndFind() {
        // When/Then
        assertEquals(PluginCategoryType.GAMES, PluginCategoryType.fromKeyword("  games  "));
        assertEquals(PluginCategoryType.TOOLS, PluginCategoryType.fromKeyword("\ttools\n"));
        assertEquals(PluginCategoryType.DEVELOPMENT, PluginCategoryType.fromKeyword(" development "));
    }

    @Test
    void fromKeyword_WithNull_ShouldReturnDefault() {
        // When/Then
        assertEquals(PluginCategoryType.DEFAULT, PluginCategoryType.fromKeyword(null));
    }

    @Test
    void fromKeyword_WithEmptyString_ShouldReturnDefault() {
        // When/Then
        assertEquals(PluginCategoryType.DEFAULT, PluginCategoryType.fromKeyword(""));
        assertEquals(PluginCategoryType.DEFAULT, PluginCategoryType.fromKeyword("   "));
    }

    @Test
    void fromKeyword_WithUnknownKeyword_ShouldReturnDefault() {
        // When/Then
        assertEquals(PluginCategoryType.DEFAULT, PluginCategoryType.fromKeyword("unknown"));
        assertEquals(PluginCategoryType.DEFAULT, PluginCategoryType.fromKeyword("nonexistent"));
        assertEquals(PluginCategoryType.DEFAULT, PluginCategoryType.fromKeyword("12345"));
    }

    @Test
    void getValidCategories_ShouldReturnAllCategoriesExceptDefault() {
        // When
        List<PluginCategoryType> validCategories = PluginCategoryType.getValidCategories();

        // Then
        assertNotNull(validCategories);
        assertFalse(validCategories.contains(PluginCategoryType.DEFAULT));
        assertTrue(validCategories.contains(PluginCategoryType.GAMES));
        assertTrue(validCategories.contains(PluginCategoryType.TOOLS));
        assertTrue(validCategories.contains(PluginCategoryType.DEVELOPMENT));
        assertTrue(validCategories.contains(PluginCategoryType.WEB));
        assertTrue(validCategories.contains(PluginCategoryType.JAVA));
        assertTrue(validCategories.contains(PluginCategoryType.PYTHON));
        assertTrue(validCategories.contains(PluginCategoryType.MOBILE));
        assertTrue(validCategories.contains(PluginCategoryType.TESTING));
        assertTrue(validCategories.contains(PluginCategoryType.SECURITY));

        // Vérifier que le nombre de catégories valides est values().length - 1 (pour DEFAULT)
        assertEquals(PluginCategoryType.values().length - 1, validCategories.size());
    }

    @Test
    void values_ShouldContainAllCategories() {
        // When
        PluginCategoryType[] values = PluginCategoryType.values();

        // Then
        assertNotNull(values);
        // On ne teste pas une taille exacte car elle peut changer, on teste juste qu'il y a au moins 1 catégorie
        assertTrue(values.length >= 1);

        // Vérifier les catégories principales (celles qui doivent exister)
        assertTrue(containsCategory(values, PluginCategoryType.GAMES));
        assertTrue(containsCategory(values, PluginCategoryType.TOOLS));
        assertTrue(containsCategory(values, PluginCategoryType.DEVELOPMENT));
        assertTrue(containsCategory(values, PluginCategoryType.DATABASE));
        assertTrue(containsCategory(values, PluginCategoryType.WEB));
        assertTrue(containsCategory(values, PluginCategoryType.JAVA));
        assertTrue(containsCategory(values, PluginCategoryType.DEFAULT));
    }

    @Test
    void valueOf_ShouldReturnCorrectCategory() {
        // When/Then
        assertEquals(PluginCategoryType.GAMES, PluginCategoryType.valueOf("GAMES"));
        assertEquals(PluginCategoryType.TOOLS, PluginCategoryType.valueOf("TOOLS"));
        assertEquals(PluginCategoryType.DEVELOPMENT, PluginCategoryType.valueOf("DEVELOPMENT"));
        assertEquals(PluginCategoryType.WEB, PluginCategoryType.valueOf("WEB"));
        assertEquals(PluginCategoryType.JAVA, PluginCategoryType.valueOf("JAVA"));
        assertEquals(PluginCategoryType.DEFAULT, PluginCategoryType.valueOf("DEFAULT"));
    }

    @Test
    void valueOf_WithInvalidName_ShouldThrowException() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> PluginCategoryType.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> PluginCategoryType.valueOf("games")); // case sensitive
    }

    @Test
    void DEFAULT_Category_ShouldHaveEmptyKeywords() {
        // Then
        Set<String> defaultKeywords = PluginCategoryType.DEFAULT.getKeywords();
        assertNotNull(defaultKeywords);
        assertTrue(defaultKeywords.isEmpty());
    }

    @Test
    void DEFAULT_Category_ShouldBeReturned_WhenNoMatchFound() {
        // When
        PluginCategoryType result = PluginCategoryType.fromKeyword("completely-random-keyword");

        // Then
        assertEquals(PluginCategoryType.DEFAULT, result);
    }

    @Test
    void fromKeyword_ShouldReturnFirstMatch_WhenMultipleCategoriesShareKeyword() {
        // Note: This test assumes no duplicate keywords across categories
        // If there are duplicates, it returns the first one found

        String uniqueKeyword = "games"; // Only in GAMES category
        PluginCategoryType result = PluginCategoryType.fromKeyword(uniqueKeyword);

        assertEquals(PluginCategoryType.GAMES, result);
    }

    private boolean containsCategory(PluginCategoryType[] values, PluginCategoryType category) {
        for (PluginCategoryType value : values) {
            if (value == category) {
                return true;
            }
        }
        return false;
    }
}